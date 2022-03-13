package calang;

import calang.instructions.*;
import calang.model.*;
import calang.model.algebra.GraphLinks;
import calang.model.algebra.WithScopeAndOperators;
import calang.model.operator.OperatorMap;
import calang.model.operator.WritableOperatorMap;
import calang.model.types.WritableTypeMap;
import calang.types.*;
import calang.types.dummy.Dummy;

import java.util.*;
import java.util.stream.*;

import static calang.model.operator.WritableOperatorMap.newWritableOperatorMap;
import static calang.model.types.WritableTypeMap.newWritableTypeMap;
import static calang.rejections.Rejections.*;
import static java.util.Collections.*;
import static java.util.function.Predicate.not;

public class Calang {
    final WritableTypeMap typesMap;
    final WritableOperatorMap operatorsMap;

    protected Calang() {
        typesMap = newWritableTypeMap();
        operatorsMap = newWritableOperatorMap();
        {
            addType("INTEGER", IntegerValue.class);
            addType("BYTES", BytesValue.class);
            addType("BOOLEAN", BooleanValue.class);
            addType("PROGRAM", ProgramValue.class);
        }
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

    public final <T extends TypedValue<T>> void addType(String typeIdentifier, Class<T> type) {
        typesMap.registerType(typeIdentifier, type);
    }

    public final <T extends TypedValue<T>, R extends TypedValue<R>> void addOperator(Class<T> clz, String operatorName,
                                                               Class<R> returnType,
                                                               List<Class<? extends TypedValue<?>>> typeChecker) {
        operatorsMap.registerOperator(clz, operatorName, returnType, typeChecker);
    }
    public final <T extends TypedValue<T>, R extends TypedValue<R>> void addOperator(Class<T> clz, String operatorName,
                                                               Class<R> returnType, Class<? extends TypedValue<?>> typeChecker) {
        operatorsMap.registerOperator(clz, operatorName, returnType, typeChecker);
    }

    /******************************************************************** */

    public interface PreInstruction {
        List<String> transpile(Program<PreInstruction> program);
    }

    protected Program<PreInstruction> parse(List<String> lines) {
        if (lines.stream().anyMatch(String::isBlank)) {
            return parse(lines.stream().filter(not(String::isBlank)).toList());
        }
        Scope scope;
        {
            HashSet<Scope.Declaration<?>> variables;
            HashSet<Scope.Declaration<?>> inputs;
            HashSet<Scope.Declaration<?>> outputs;
            {
                variables = new HashSet<>();
                inputs = new HashSet<>();
                outputs = new HashSet<>();
                lines.stream().takeWhile(l -> l.startsWith("DECLARE")).forEach(line -> {
                    var tokens = line.trim().split("\s+");
                    assert tokens[0].equals("DECLARE");
                    var declaration = new Scope.Declaration<Dummy>(
                            tokens[tokens.length - 2],
                            typesMap.typeForSymbol(tokens[tokens.length - 1])
                    );
                    variables.add(declaration);
                    if (tokens.length == 4) {
                        if ("INPUT".equals(tokens[1])) inputs.add(declaration);
                        else if ("OUTPUT".equals(tokens[1])) outputs.add(declaration);
                    }
                });
            }
            scope = new Scope() {
                @Override
                public Set<Scope.Declaration<?>> declarations() {
                    return unmodifiableSet(variables);
                }

                @Override
                public Set<Scope.Declaration<?>> inputDeclarations() {
                    return unmodifiableSet(inputs);
                }

                @Override
                public Set<Scope.Declaration<?>> outputDeclarations() {
                    return unmodifiableSet(outputs);
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
                        class Impl implements ComptInstructionMk<Void>, WithScopeAndOperators {
                            @Override
                            public Scope scope() {
                                return scope;
                            }

                            @Override
                            public OperatorMap operators() {
                                return operatorsMap;
                            }

                            @Override
                            public Void computeInstruction(String targetSymbol, String baseSymbol, String operator, List<String> parameterSymbols) {
                                assertOperatorUsageValid(baseSymbol, targetSymbol, operator, parameterSymbols);
                                return null;
                            }
                        } new Impl().makeInstruction(tokens);
                    } else if (mk instanceof CallInstructionMk<PreInstruction> callMk) {
                        record Combined(PreInstruction wrapped) implements GraphLinks.CallPreInstruction, PreInstruction {
                            @Override
                            public List<String> transpile(Program<PreInstruction> p) {
                                return Combined.this.wrapped.transpile(p);
                            }
                        }
                        mk = t -> new Combined(callMk.makeInstruction(t));
                    } else if (mk instanceof PerformInstructionMk<PreInstruction> performMk) {
                        record Combined(PreInstruction wrapped, List<String> dependentParagraphs) implements GraphLinks.PerformPreInstruction, PreInstruction {
                            @Override
                            public List<String> transpile(Program<PreInstruction> p) {
                                return Combined.this.wrapped.transpile(p);
                            }
                        }
                        class Spy implements PerformInstructionMk<Void> {
                            List<String> paragraphNames;
                            @Override
                            public Void performInstruction(String paragraphName, String alternativeParagraphName, String booleanValueSymbol, boolean isLoop, boolean isContraCondition) {
                                paragraphNames = Stream.of(paragraphName, alternativeParagraphName)
                                        .filter(Objects::nonNull)
                                        .toList();
                                return null;
                            }

                            List<String> dependentParagraphs(String[] t) {
                                makeInstruction(t);
                                assert paragraphNames != null;
                                return paragraphNames;
                            }
                        } var dependentParagraphs = new Spy().dependentParagraphs(tokens);
                        mk = t -> new Combined(performMk.makeInstruction(t), dependentParagraphs);
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

        return new Program<PreInstruction>() {
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

    protected List<String> transpilePerformInstruction(Program<PreInstruction> program, String paragraphName, String altParagraphName, String booleanValueSymbol, boolean isLoop, boolean contraCondition) {
        throw NON_TRANSPILED_INSTRUCTION.error("PERFORM");
    }

    protected List<String> transpileStoreInstruction(Program<PreInstruction> program, String sourceSymbol, String targetSymbol) {
        throw NON_TRANSPILED_INSTRUCTION.error("STORE");
    }

    protected List<String> transpileComptInstruction(Program<PreInstruction> program, String targetSymbol, String baseSymbol, String operator, List<String> arguments) {
        throw NON_TRANSPILED_INSTRUCTION.error("COMPT");
    }

    protected List<String> transpilePrintInstruction(Program<PreInstruction> program, List<String> tokens) {
        throw NON_TRANSPILED_INSTRUCTION.error("PRINT");
    }

    protected List<String> transpileCallInstruction(Program<PreInstruction> program, String programName, List<VariableBinding> in, List<VariableBinding> out) {
        throw NON_TRANSPILED_INSTRUCTION.error("CALL");
    }

}
