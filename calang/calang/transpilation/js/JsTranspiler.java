package calang.transpilation.js;

import calang.VariableBinding;
import calang.model.bricks.Paragraphs;
import calang.model.bricks.Scope;
import calang.model.types.TypedValue;
import calang.transpilation.Transpiler;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.unmodifiableList;
import static java.util.Comparator.comparing;

public interface JsTranspiler extends Transpiler<List<String>> {

    String programName();

    Paragraphs paragraphs();

    Scope scope();

    default String programOf(String programSymbol) {
        return isSymbolPresent(programSymbol) ? "%s.bindWith".formatted(getValue(programSymbol)) : "new %s".formatted(programSymbol);
    }

    default String runProgram(String programSymbol, List<VariableBinding> inputs, List<VariableBinding> outputs) {
        programSymbol = programOf(programSymbol);
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
                        linesToWrite.add(instructionOf(instr));
                    linesToWrite.add("  },");
                }
                {
                    linesToWrite.add("  run: async function() { %s; this.printer.flush(); return { %s }; }".formatted(
                            callParagraph(paragraphs().headParagraph(), paragraphs().isParagraphAsynchronous(paragraphs().headParagraph())),
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

    /******************************************************************************* */

    default String transpileCallInstruction(String programSymbol, List<VariableBinding> inputs, List<VariableBinding> outputs) {
        return runProgram(programSymbol, inputs, outputs);
    }

    default String transpileComptInstruction(String targetSymbol, String baseSymbol, String operator, List<String> parameterSymbols) {
        return setValue(targetSymbol, sendMessage(baseSymbol, operator, parameterSymbols));
    }

    default String transpileStoreInstruction(String targetSymbol, String sourceSymbol) {
        return setValueOrBytes(targetSymbol, sourceSymbol);
    }

    default String transpilePrintInstruction(List<String> tokens) {
        return print(tokens);
    }

    default String transpilePerformInstruction(String paragraphName, String altParagraphName, String booleanValueSymbol, boolean isLoop, boolean isContraCondition) {
        String line;
        var isAsync = paragraphs().isParagraphAsynchronous(paragraphName);

        if(booleanValueSymbol == null)
            line = callParagraph(paragraphName, isAsync);
        else if (isLoop)
            line = printWhile(booleanValueSymbol, callParagraph(paragraphName, isAsync));
        else if (isContraCondition)
            line = printIfNot(booleanValueSymbol, callParagraph(paragraphName, isAsync));
        else if (altParagraphName != null)
            line = printIfElse(booleanValueSymbol, callParagraph(paragraphName, isAsync), callParagraph(altParagraphName, paragraphs().isParagraphAsynchronous(altParagraphName)));
        else
            line = printIf(booleanValueSymbol, callParagraph(paragraphName, isAsync));
        return line;
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

    default String callParagraph(String paragraphName, boolean async) {
        var base = "%s()".formatted(fVar(fPar(paragraphName)));
        if(async)
            return "await " + base;
        return base;
    }

    default String getValue(String symbol) {
        return "%s.getValue()".formatted(fVar(symbol));
    }

    default String printWhile(String booleanSymbol, String instruction) {
        return "while(%s) %s;".formatted(getValue(booleanSymbol), instruction);
    }

    default String printIf(String booleanSymbol, String instruction) {
        return "if(%s) %s;".formatted(getValue(booleanSymbol), instruction);
    }

    default String printIfElse(String booleanSymbol, String instruction, String altInstruction) {
        return "if(%s) %s; else %s;".formatted(getValue(booleanSymbol), instruction, altInstruction);
    }

    default String printIfNot(String booleanSymbol, String instruction) {
        return "if(! %s) %s;".formatted(getValue(booleanSymbol), instruction);
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
