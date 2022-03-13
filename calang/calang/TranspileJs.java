package calang;

import calang.model.Paragraph;
import calang.model.Program;
import calang.model.TypedValue;
import calang.model.algebra.GraphLinks;

import java.util.*;
import java.util.stream.*;

import static java.util.function.Predicate.not;

public class TranspileJs extends Calang {

    protected String transpileType(Class<? extends TypedValue<?>> value) {
        return "Calang['%s']".formatted(value.getSimpleName());
    }

    private static String fPar(String paragraphName) {
        return "__%s".formatted(paragraphName);
    }

    private static String fVar(String varName) {
        return "this.%s".formatted(varName);
    }

    protected List<String> transpile(String programName, List<String> lines) {
        lines = lines.stream()
                .filter(not(String::isBlank))
                .map(s -> s.replaceAll("[\n\r]+", ""))
                .toList();
        return transpile(programName, parse(lines));
    }

    private Set<Paragraph<PreInstruction>> __asynchronousParagraphs;
    private Program<PreInstruction> __program;
    private boolean isAsynchronous(Paragraph<PreInstruction> p) {
        return __asynchronousParagraphs.contains(p);
    }
    private boolean isAsynchronous(String pName) {
        return isAsynchronous(__program.paragraph(pName));
    }
    private String callParagraph(String paragraphName) {
        return "%s %s()".formatted(isAsynchronous(paragraphName) ? "await": "", fVar(fPar(paragraphName)));
    }

    @Override
    protected List<String> transpile(String programName, Program<PreInstruction> program) {
        var scope = program.scope();
        __program = program;
        __asynchronousParagraphs = GraphLinks.asynchronousParagraphs(program);
        var inputs = new HashSet<>(program.scope().inputSymbols());

        var linesToWrite = new ArrayList<String>();
        linesToWrite.add("var %s = (function() {%nvar def = function({ %s }) { this.printer = new Print();".formatted(programName, String.join(", ", inputs)));
        for (var s : scope.declarations()) {
            linesToWrite.add("  %s = %s.newInstance();".formatted(fVar(s.identifier()), transpileType(s.type())));
            if (inputs.contains(s.identifier())) linesToWrite.add("    %s.setValue(%s);".formatted(fVar(s.identifier()), s.identifier()));
        }
        linesToWrite.add("};%sdef.prototype = {".formatted(System.lineSeparator()));
        for (var name : program.paragraphNames()) {
            var paragraph = program.paragraph(name);
            assert paragraph != null;
            linesToWrite.add("  %s: %s function() {".formatted(fPar(name), isAsynchronous(paragraph) ? "async" : ""));
            for (var instr : paragraph.instructions())
                linesToWrite.addAll(instr.transpile(program));
            linesToWrite.add("  },");
        }
        {
            linesToWrite.add("  run: async function() { %s; this.printer.flush(); return { %s }; }\n};".formatted(
                    callParagraph(program.headParagraphName()),
                    program.scope().outputSymbols().stream()
                            .map(t -> "%s:%s".formatted(t, fVar(t)))
                            .collect(Collectors.joining(", "))));
        }
        linesToWrite.add("return def; })();");
        return linesToWrite;
    }

    @Override
    protected List<String> transpilePerformInstruction(Program<PreInstruction> program, String paragraphName, String altParagraphName, String booleanValueSymbol, boolean isLoop, boolean contraCondition) {
        assert paragraphName != null;

        String line = callParagraph(paragraphName);
        decorator: {
            if(booleanValueSymbol == null) break decorator;

            var flagName = fVar(booleanValueSymbol);
            if (isLoop)
                line = "while(%s.getValue()) %s;".formatted(flagName, line);
            else if (contraCondition)
                line = "if(! %s.getValue()) %s;".formatted(flagName, line);
            else if (altParagraphName != null)
                line = "if(%s.getValue()) %s; else %s;".formatted(flagName, line, callParagraph(altParagraphName));
        }
        return Collections.singletonList(line);
    }

    @Override
    protected List<String> transpileStoreInstruction(Program<PreInstruction> program, String targetSymbol, String sourceSymbol) {
        var target = fVar(targetSymbol);
        String value = program.scope().isSymbolDeclared(sourceSymbol) ? fVar(sourceSymbol) : "\"%s\"".formatted(sourceSymbol);
        var line = "%s.setValue(%s);".formatted(target, value);
        return Collections.singletonList(line);
    }

    @Override
    protected List<String> transpileComptInstruction(Program<PreInstruction> program, String targetSymbol, String baseSymbol, String operator, List<String> arguments) {
        var target = fVar(targetSymbol);
        var base = fVar(baseSymbol);
        var args = arguments.stream().map(TranspileJs::fVar).collect(Collectors.joining(", "));

        var line = "%s.setValue(%s.sendMessage(\"%s\", [%s]));".formatted(target, base, operator, args);
        return Collections.singletonList(line);
    }

    @Override
    protected List<String> transpilePrintInstruction(Program<PreInstruction> program, List<String> tokens) {
        var scope = program.scope();
        var words = tokens.stream().map(t -> scope.isSymbolDeclared(t) ? "${%s.getValue()}".formatted(fVar(t)) : t).collect(Collectors.joining(" "));
        var line = "this.printer.append(`%s`);".formatted(words);
        return Collections.singletonList(line);
    }

    @Override
    protected List<String> transpileCallInstruction(Program<PreInstruction> program, String programName, List<VariableBinding> in, List<VariableBinding> out) {
        var scope = program.scope();
        var lines = new ArrayList<String>();
        String programIdentifier = scope.isSymbolDeclared(programName) ? "%s.getValue().bindWith".formatted(fVar(programName)) : "new %s".formatted(programName);
        lines.add("await %s({ %s }).run()".formatted(programIdentifier, in.stream().map(b -> "%s:%s".formatted(b.childSymb(), fVar(b.parentSymb()))).collect(Collectors.joining(","))));
        {
            lines.add(".then(__ => {");
            for (var ob : out)
                lines.add("  %s.setValue(__.%s);".formatted(fVar(ob.parentSymb()), ob.childSymb()));
            lines.add("})");
            lines.add(";");
        }
        return lines;
    }

}

