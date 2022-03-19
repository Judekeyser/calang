package calang.transpilation;

import calang.VariableBinding;
import calang.model.bricks.instructions.*;

import java.util.List;

import static calang.rejections.Rejections.UNRECOGNIZED_INSTRUCTION_TOKEN;

public interface Transpiler<T> {
    T transpile();

    String transpileCallInstruction(String programSymbol, List<VariableBinding> inputs, List<VariableBinding> outputs);

    String transpileComptInstruction(String targetSymbol, String baseSymbol, String operator, List<String> parameterSymbols);

    String transpileStoreInstruction(String targetSymbol, String sourceSymbol);

    String transpilePrintInstruction(List<String> tokens);

    String transpilePerformInstruction(String paragraphName, String altParagraphName, String booleanValueSymbol, boolean isLoop, boolean isContraCondition);

    default String instructionOf(String[] tokens) {
        abstract class Template implements InstructionMk<String> {
            public String[] tokens() {
                return tokens;
            }
        }
        return (switch (tokens[0]) {
            case "PERFORM" -> {
                class Impl extends Template implements PerformInstructionMk<String> {
                    @Override
                    public String performInstruction(String paragraphName, String altParagraphName, String booleanValueSymbol, boolean isLoop, boolean isContraCondition) {
                        return Transpiler.this.transpilePerformInstruction(paragraphName, altParagraphName, booleanValueSymbol, isLoop, isContraCondition);
                    }
                } yield new Impl();
            }
            case "PRINT" -> {
                class Impl extends Template implements PrintInstructionMk<String> {
                    @Override
                    public String printInstruction(List<String> tokens) {
                        return Transpiler.this.transpilePrintInstruction(tokens);
                    }
                } yield new Impl();
            }
            case "STORE" -> {
                class Impl extends Template implements StoreInstructionMk<String> {
                    @Override
                    public String storeInstruction(String targetSymbol, String sourceSymbol) {
                        return Transpiler.this.transpileStoreInstruction(targetSymbol, sourceSymbol);
                    }
                } yield new Impl();
            }
            case "COMPT" -> {
                class Impl extends Template implements ComptInstructionMk<String> {
                    @Override
                    public String computeInstruction(String targetSymbol, String baseSymbol, String operator, List<String> parameterSymbols) {
                        return Transpiler.this.transpileComptInstruction(targetSymbol, baseSymbol, operator, parameterSymbols);
                    }
                } yield new Impl();
            }
            case "CALL" -> {
                class Impl extends Template implements CallInstructionMk<String> {
                    @Override
                    public String callInstruction(String programSymbol, List<VariableBinding> inputs, List<VariableBinding> outputs) {
                        return Transpiler.this.transpileCallInstruction(programSymbol, inputs, outputs);
                    }
                } yield new Impl();
            }
            default -> throw UNRECOGNIZED_INSTRUCTION_TOKEN.error(tokens[0]);
        }) .makeInstruction();
    }
}
