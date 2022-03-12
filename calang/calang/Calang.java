package calang;

import calang.instructions.*;
import calang.scopes.Scope;
import calang.scopes.operator.WritableOperatorMap;
import calang.scopes.Operator;
import calang.types.*;

import java.util.*;
import java.util.stream.*;

import static calang.rejections.Rejections.*;
import static java.util.Collections.*;
import static java.util.Collections.unmodifiableList;
import static java.util.List.copyOf;
import static java.util.function.Predicate.not;

public class Calang {
    final Map<String, Class<? extends TypedValue<?>>> TOKENS;
    final WritableOperatorMap operatorsMap;

    protected Calang() {
        TOKENS = new HashMap<>(Map.of(
                "INTEGER", IntegerValue.class,
                "BYTES", BytesValue.class,
                "BOOLEAN", BooleanValue.class,
                "PROGRAM", ProgramValue.class
        ));
        operatorsMap = WritableOperatorMap.ofMap();
        {
            addOperator(IntegerValue.class, "NEQ", BooleanValue.class, singletonList(IntegerValue.class));
            addOperator(IntegerValue.class, "PREC", IntegerValue.class, emptyList());
            addOperator(IntegerValue.class, "SUCC", IntegerValue.class, emptyList());
        }
        {
            addOperator(BytesValue.class, "|.|", IntegerValue.class, emptyList());
        }
        {
            addOperator(BooleanValue.class, "NEGATE", BooleanValue.class, emptyList());
            addOperator(BooleanValue.class, "AND",BooleanValue.class, BooleanValue.class);
            addOperator(BooleanValue.class, "OR", BooleanValue.class, BooleanValue.class);
            addOperator(BooleanValue.class, "XAND", BooleanValue.class, BooleanValue.class);
            addOperator(BooleanValue.class, "XOR", BooleanValue.class, BooleanValue.class);
            addOperator(BooleanValue.class, "IMPLIES", BooleanValue.class, singletonList(IntegerValue.class));
        }
    }

    public final <T extends TypedValue<T>> void addType(String typeIdentifier, Class<T> typeFactory) {
        TOKENS.put(typeIdentifier, typeFactory);
    }

    public final <T extends TypedValue<T>, R extends TypedValue<R>> void addOperator(Class<T> clz, String operatorName,
                                                               Class<R> returnType,
                                                               List<Class<? extends TypedValue<?>>> typeChecker) {
        operatorsMap.addOperator(clz, operatorName, returnType, typeChecker);
    }
    public final <T extends TypedValue<T>, R extends TypedValue<R>> void addOperator(Class<T> clz, String operatorName,
                                                               Class<R> returnType, Class<? extends TypedValue<?>> typeChecker) {
        operatorsMap.addOperator(clz, operatorName, returnType, typeChecker);
    }

    /******************************************************************** */

    public interface PreInstruction {
        List<String> transpile(Scope scope);
    }

    protected Program<PreInstruction> parse(List<String> lines) {
        if (lines.stream().anyMatch(String::isBlank)) {
            return parse(lines.stream().filter(not(String::isBlank)).toList());
        }
        Scope scope;
        {
            HashMap<String, Class<? extends TypedValue<?>>> variables;
            ArrayList<String> inputs;
            ArrayList<String> outputs;
            {
                variables = new HashMap<>();
                inputs = new ArrayList<>();
                outputs = new ArrayList<>();
                lines.stream().takeWhile(l -> l.startsWith("DECLARE")).forEach(line -> {
                    var tokens = line.trim().split("\s+");
                    assert tokens[0].equals("DECLARE");
                    var varName = tokens[tokens.length - 2];
                    var varType = tokens[tokens.length - 1];
                    var variable = Objects.requireNonNull(TOKENS.get(varType), "Unable to resolve type %s".formatted(varType));
                    variables.put(varName, variable);
                    if (tokens.length == 4) {
                        if ("INPUT".equals(tokens[1])) inputs.add(varName);
                        else if ("OUTPUT".equals(tokens[1])) outputs.add(varName);
                    }
                });
            }
            scope = new Scope() {
                @Override
                @SuppressWarnings("unchecked")
                public <T extends TypedValue<T>> Class<T> typeOf(String token) {
                    var type = variables.get(token);
                    if (type == null) throw UNKNOWN_VARIABLE.error(token);
                    return (Class<T>) type;
                }

                @Override
                public List<String> symbolList() {
                    return copyOf(variables.keySet());
                }

                @Override
                public List<String> inputsList() {
                    return unmodifiableList(inputs);
                }

                @Override
                public List<String> outputsList() {
                    return unmodifiableList(outputs);
                }

                @Override
                public <T extends TypedValue<T>> Operator<T> maybeOperator(Class<T> typedValue, String operatorName) {
                    return operatorsMap.maybeOperator(typedValue, operatorName);
                }
            };
        }

        record Par(int lineIndex, String name, List<PreInstruction> instructions) implements Paragraph<PreInstruction> {}

        List<Par> paragraphs;
        Par headParagraph;
        {
            class InstructionChecker {
                InstructionMk<PreInstruction> uncheckedPreInstruction(String[] tokens) {
                    return switch (tokens[0]) {
                        case "PERFORM" -> (PerformInstructionMk<PreInstruction>) (_1, _2, _3, _4, _5) -> __ -> transpilePerformInstruction(__, _1, _2, _3, _4, _5);
                        case "PRINT" -> (PrintInstructionMk<PreInstruction>) _1 -> __ -> transpilePrintInstruction(__, _1);
                        case "STORE" -> (StoreInstructionMk<PreInstruction>) (_1, _2) -> __ -> transpileStoreInstruction(__, _1, _2);
                        case "COMPT" -> (ComptInstructionMk<PreInstruction>) (_1, _2, _3, _4) -> __ -> transpileComptInstruction(__, _1, _2, _3, _4);
                        case "CALL" -> (CallInstructionMk<PreInstruction>) (_1, _2, _3) -> __ -> transpileCallInstruction(__, _1, _2, _3);
                        default -> throw UNRECOGNIZED_INSTRUCTION_TOKEN.error(tokens[0]);
                    };
                }
                PreInstruction checkedPreInstruction(String line) {
                    if (line.startsWith("  ")) return checkedPreInstruction(line.substring(2));
                    assert line.indexOf(" ") > 0 : "Malformed instruction line |%s|".formatted(line);
                    var tokens = line.trim().split("\s+");

                    var mk = uncheckedPreInstruction(tokens);
                    if (mk instanceof ComptInstructionMk<PreInstruction> comptMk) {
                        // Going to recreate a custom ComptInstructionMk that auto-validates itself
                        class Impl implements ComptInstructionMk<Void> {
                            @Override
                            public Void computeInstruction(String targetSymbol, String baseSymbol, String operator, List<String> parameterSymbols) {
                                scope.assertOperatorUsageValid(baseSymbol, targetSymbol, operator, parameterSymbols);
                                return null;
                            }
                        } new Impl().makeInstruction(tokens);
                    }
                    return mk.makeInstruction(tokens);
                }
            } var checker = new InstructionChecker();
            paragraphs = IntStream.range(0, lines.size())
                    .dropWhile(i -> lines.get(i).startsWith("DECLARE"))
                    .filter(i -> !lines.get(i).startsWith("  "))
                    .mapToObj(i -> new Par(i, lines.get(i).substring(0, lines.get(i).length() - 1), IntStream.range(i + 1, lines.size()).takeWhile(j -> lines.get(j).startsWith("  ")).mapToObj(lines::get).map(checker::checkedPreInstruction).toList())).sorted(Comparator.comparing(Par::name)).toList();

            headParagraph = paragraphs.stream().min(Comparator.comparingInt(Par::lineIndex)).orElseThrow(NO_PARAGRAPH_FOUND::error);
        }

        return new Program<>() {
            @Override
            public String headParagraphName() {
                return headParagraph.name();
            }

            @Override
            public Paragraph<PreInstruction> paragraph(String name) {
                return paragraphs.stream().filter(__ -> __.name().equals(name)).findFirst().orElseThrow(() -> UNDEFINED_PARAGRAPH.error(name));
            }

            @Override
            public List<String> paragraphNames() {
                return paragraphs.stream().map(Par::name).toList();
            }

            @Override
            public Scope scope() {
                return scope;
            }
        };
    }

    /******************************************************************** */

    protected List<String> transpile(String programName, Program<PreInstruction> program) {
        throw NON_TRANSPILED_PROGRAM.error(programName);
    }

    protected List<String> transpilePerformInstruction(Scope scope, String paragraphName, String altParagraphName, String booleanValueSymbol, boolean isLoop, boolean contraCondition) {
        throw NON_TRANSPILED_INSTRUCTION.error("PERFORM");
    }

    protected List<String> transpileStoreInstruction(Scope scope, String sourceSymbol, String targetSymbol) {
        throw NON_TRANSPILED_INSTRUCTION.error("STORE");
    }

    protected List<String> transpileComptInstruction(Scope scope, String targetSymbol, String baseSymbol, String operator, List<String> arguments) {
        throw NON_TRANSPILED_INSTRUCTION.error("COMPT");
    }

    protected List<String> transpilePrintInstruction(Scope scope, List<String> tokens) {
        throw NON_TRANSPILED_INSTRUCTION.error("PRINT");
    }

    protected List<String> transpileCallInstruction(Scope scope, String programName, List<VariableBinding> in, List<VariableBinding> out) {
        throw NON_TRANSPILED_INSTRUCTION.error("CALL");
    }

}
