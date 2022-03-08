package calang;

import calang.types.TypedValue;

import java.util.*;
import java.util.stream.*;

import static java.util.function.Predicate.not;

public class TranspileJs extends Calang {

    protected String transpileType(Class<? extends TypedValue<?, ?>> value) {
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

    @Override
    protected List<String> transpile(String programName, Program program) {
        var scope = program.scope();
        var inputs = new HashSet<>(program.getDeclaredInputs());

        var linesToWrite = new ArrayList<String>();
        linesToWrite.add("var %s = (function() {%nvar def = function({ %s }) { this.printer = new Print();".formatted(programName, String.join(", ", inputs)));
        for (var s : scope.symbolList()) {
            linesToWrite.add("  %s = %s.newInstance();".formatted(fVar(s), transpileType(scope.getOrDie(s))));
            if (inputs.contains(s)) linesToWrite.add("    %s.setValue(%s);".formatted(fVar(s), s));
        }
        linesToWrite.add("};%ndef.prototype = {".formatted());
        for (var name : program.paragraphNames()) {
            linesToWrite.add("  %s: async function() {".formatted(fPar(name)));
            var paragraph = program.paragraph(name);
            assert paragraph != null;
            for (var instr : paragraph.instructions())
                linesToWrite.addAll(instr.transpile(scope));
            linesToWrite.add("  },");
        }
        {
            linesToWrite.add("  run: async function() { await %s(); this.printer.flush(); return { %s }; }\n};".formatted(fVar(fPar(program.headParagraphName())), program.getDeclaredOutputs().stream().map(t -> "%s:%s".formatted(t, fVar(t))).collect(Collectors.joining(", "))));
        }
        linesToWrite.add("return def; })();");
        return linesToWrite;
    }

    @Override
    protected List<String> transpilePerformInstruction(Scope scope, String paragraphName, String altParagraphName, String booleanValueSymbol, boolean isLoop, boolean contraCondition) {
        assert paragraphName != null;
        var parName = fVar(fPar(paragraphName));
        var altParName = altParagraphName == null ? null : fVar(fPar(altParagraphName));
        var flagName = booleanValueSymbol == null ? null : fVar(booleanValueSymbol);

        String line = "await %s();".formatted(parName);
        decorator: {
            if(flagName == null) break decorator;

            if (isLoop)
                line = "while(%s.getValue()) %s".formatted(flagName, line);
            else if (contraCondition)
                line = "if(! %s.getValue()) %s".formatted(flagName, line);
            else if (altParagraphName != null)
                line = "if(%s.getValue()) %s else await %s();".formatted(flagName, line, altParName);
        }
        return Collections.singletonList(line);
    }

    @Override
    protected List<String> transpileStoreInstruction(Scope scope, String targetSymbol, String sourceSymbol) {
        var target = fVar(targetSymbol);
        String value;
        {
            if (scope.symbol(sourceSymbol).isPresent()) value = fVar(sourceSymbol);
            else value = "\"%s\"".formatted(sourceSymbol);
        }
        var line = "%s.setValue(%s);".formatted(target, value);
        return Collections.singletonList(line);
    }

    @Override
    protected List<String> transpileComptInstruction(Scope scope, String targetSymbol, String baseSymbol, String operator, List<String> arguments) {
        var target = fVar(targetSymbol);
        var base = fVar(baseSymbol);
        var args = arguments.stream().map(TranspileJs::fVar).collect(Collectors.joining(", "));

        var line = "%s.setValue(%s.sendMessage(\"%s\", [%s]));".formatted(target, base, operator, args);
        return Collections.singletonList(line);
    }

    @Override
    protected List<String> transpilePrintInstruction(Scope scope, List<String> tokens) {
        var words = tokens.stream().map(t -> scope.symbol(t).isPresent() ? "${%s.getValue()}".formatted(fVar(t)) : t).collect(Collectors.joining(" "));
        var line = "this.printer.append(`%s`);".formatted(words);
        return Collections.singletonList(line);
    }

    @Override
    protected List<String> transpileCallInstruction(Scope scope, String programName, List<VariableBinding> in, List<VariableBinding> out) {
        var lines = new ArrayList<String>();
        String programIdentifier;
        {
            programIdentifier = scope.symbol(programName).isPresent() ? "%s.getValue().bindWith".formatted(fVar(programName)) : "new %s".formatted(programName);
        }
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

