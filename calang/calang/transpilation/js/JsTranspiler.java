package calang.transpilation.js;

import calang.VariableBinding;
import calang.model.bricks.Paragraphs;
import calang.model.bricks.Scope;
import calang.model.bricks.instructions.*;
import calang.model.types.TypedValue;
import calang.transpilation.Transpiler;

import java.util.*;
import java.util.stream.Collectors;

import static calang.rejections.Rejections.UNRECOGNIZED_INSTRUCTION_TOKEN;
import static java.util.Collections.unmodifiableList;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;

public interface JsTranspiler extends Transpiler<List<String>> {

    String programName();

    Paragraphs paragraphs();

    Scope scope();

    default String programOf(String programSymbol) {
        return isSymbolPresent(programSymbol) ? "%s.bindWith".formatted(getValue(programSymbol)) : "new %s".formatted(programSymbol);
    }

    default String runProgram(String programSymbol, List<VariableBinding> inputs, List<VariableBinding> outputs) {
        return """
                await %s({
                %s
                }).run().then(__ => {
                %s
                });
                """
                .formatted(
                        programSymbol,
                        inputs.stream()
                                .map(b -> "%s:%s".formatted(b.childSymb(), fVar(b.parentSymb())))
                                .collect(Collectors.joining(",")),
                        outputs.stream()
                                .map(ob -> setValue(ob.parentSymb(), "__.%s".formatted(ob.childSymb())))
                                .collect(Collectors.joining("\n"))
                        );
    }

    default String print(List<String> symbols) {
        return "this.printer.append(`%s`);".formatted(symbols.stream()
                .map(t -> isSymbolPresent(t) ? "${%s.getValue()}".formatted(fVar(t)) : t)
                .collect(Collectors.joining(" "))
        );
    }

    default boolean isSymbolPresent(String name) {
        return scope().maybeSymbol(name).isPresent();
    }

    default List<String> transpile() {
        var paragraphLookup = paragraphs().paragraphsByName();
        var scope = scope();
        var linesToWrite = new ArrayList<String>();

        { // Definition as closure
            linesToWrite.add("var %s = (function() {%nvar def = ".formatted(programName()));

            { // Declarations and preamble
                linesToWrite.add("function({ %s }) { this.printer = new Print();".formatted(
                        scope.inputs().stream()
                                .map(Scope.Declaration::name)
                                .sorted()
                                .collect(Collectors.joining(","))
                ));
                for (var s : (Iterable< Scope.Declaration<?>>) scope.declarations().stream().sorted(comparing(Scope.Declaration::name))::iterator) {
                    linesToWrite.add("  %s = %s.newInstance();".formatted(fVar(s.name()), transpileType(s.type())));
                    if (scope.inputs().contains(s)) linesToWrite.add("    %s.setValue(%s);".formatted(fVar(s.name()), s.name()));
                }
                linesToWrite.add("};");
            }
            { // prototype
                linesToWrite.add("def.prototype = {");
                for (var p : (Iterable<Map.Entry<String, Paragraphs.Paragraph>>) paragraphLookup.entrySet().stream().sorted(Map.Entry.comparingByKey())::iterator) {
                    linesToWrite.add("  %s: %s function() {".formatted(fPar(p.getKey()), paragraphs().isParagraphAsynchronous(p.getKey()) ? "async" : ""));
                    for (var instr : p.getValue().tokens())
                        linesToWrite.add(requireNonNull(instructionOf(instr).makeInstruction()));
                    linesToWrite.add("  },");
                }
                {
                    linesToWrite.add("  run: async function() { %s; this.printer.flush(); return { %s }; }".formatted(
                            callParagraph(paragraphs().headParagraph()),
                            scope.outputs().stream()
                                    .map(Scope.Declaration::name)
                                    .map(t -> "%s:%s".formatted(t, fVar(t)))
                                    .collect(Collectors.joining(", "))
                    ));
                }
                linesToWrite.add("};");
            }

            linesToWrite.add("return def; })();");
        }

        return unmodifiableList(linesToWrite);
    }

    default InstructionMk<String> instructionOf(String[] tokens) {
        abstract class Template implements InstructionMk<String> {
            public String[] tokens() {
                return tokens;
            }
        }
        return switch (tokens[0]) {
            case "PERFORM" -> {
                class Impl extends Template implements PerformInstructionMk<String> {
                    @Override
                    public String performInstruction(String paragraphName, String altParagraphName, String booleanValueSymbol, boolean isLoop, boolean isContraCondition) {
                        String line;

                        if(booleanValueSymbol == null)
                            line = callParagraph(paragraphName);
                        else if (isLoop)
                            line = printWhile(booleanValueSymbol, callParagraph(paragraphName));
                        else if (isContraCondition)
                            line = printIfNot(booleanValueSymbol, paragraphName);
                        else if (altParagraphName != null)
                            line = printIfElse(booleanValueSymbol, paragraphName, altParagraphName);
                        else
                            line = printIf(booleanValueSymbol, paragraphName);
                        return line;
                    }
                } yield new Impl();
            }
            case "PRINT" -> {
                class Impl extends Template implements PrintInstructionMk<String> {
                    @Override
                    public String printInstruction(List<String> tokens) {
                        return print(tokens);
                    }
                } yield new Impl();
            }
            case "STORE" -> {
                class Impl extends Template implements StoreInstructionMk<String> {
                    @Override
                    public String storeInstruction(String targetSymbol, String sourceSymbol) {
                        return setValueOrBytes(targetSymbol, sourceSymbol);
                    }
                } yield new Impl();
            }
            case "COMPT" -> {
                class Impl extends Template implements ComptInstructionMk<String> {
                    @Override
                    public String computeInstruction(String targetSymbol, String baseSymbol, String operator, List<String> parameterSymbols) {
                        return setValue(targetSymbol, sendMessage(baseSymbol, operator, parameterSymbols));
                    }
                } yield new Impl();
            }
            case "CALL" -> {
                class Impl extends Template implements CallInstructionMk<String> {
                    @Override
                    public String callInstruction(String programSymbol, List<VariableBinding> inputs, List<VariableBinding> outputs) {
                        return runProgram(programOf(programSymbol), inputs, outputs);
                    }
                } yield new Impl();
            }
            default -> throw UNRECOGNIZED_INSTRUCTION_TOKEN.error(tokens[0]);
        };
    }

    /******************************************************************************* */

    default String transpileType(Class<? extends TypedValue<?>> value) {
        return "Calang['%s']".formatted(value.getSimpleName());
    }

    default String fPar(String paragraphName) {
        return "__%s".formatted(paragraphName);
    }

    default String fVar(String varName) {
        return "this.%s".formatted(varName);
    }

    default String callParagraph(String paragraphName) {
        var base = "%s()".formatted(fVar(fPar(paragraphName)));
        if(paragraphs().isParagraphAsynchronous(paragraphName))
            return "await " + base;
        return base;
    }

    default String getValue(String symbol) {
        return "%s.getValue()".formatted(fVar(symbol));
    }

    default String printWhile(String booleanSymbol, String block) {
        return "while(%s) %s;".formatted(getValue(booleanSymbol), block);
    }

    default String printIf(String booleanSymbol, String paragraphName) {
        return "if(%s) %s;".formatted(getValue(booleanSymbol), callParagraph(paragraphName));
    }

    default String printIfElse(String booleanSymbol, String paragraphName, String alternativeParagraphName) {
        return "if(%s) %s; else %s;".formatted(getValue(booleanSymbol), callParagraph(paragraphName), callParagraph(alternativeParagraphName));
    }

    default String printIfNot(String booleanSymbol, String paragraphName) {
        return "if(! %s) %s;".formatted(getValue(booleanSymbol), callParagraph(paragraphName));
    }

    default String setValueOrBytes(String baseSymbol, String value) {
        value = isSymbolPresent(value) ? fVar(value) : "\"%s\"".formatted(value);
        return setValue(baseSymbol, value);
    }

    default String setValue(String baseSymbol, String value) {
        return "%s.setValue(%s);".formatted(fVar(baseSymbol), value);
    }

    default String sendMessage(String baseSymbol, String operator, Collection<String> argsSymbols) {
        return "%s.sendMessage(\"%s\", [%s])".formatted(
                fVar(baseSymbol),
                operator, argsSymbols.stream().map(this::fVar).collect(Collectors.joining(","))
        );
    }

}
