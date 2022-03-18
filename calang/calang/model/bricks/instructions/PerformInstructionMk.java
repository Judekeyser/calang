package calang.model.bricks.instructions;

import java.util.Arrays;

import static calang.rejections.Rejections.*;

public interface PerformInstructionMk<T> extends InstructionMk<T>, TokensBased {

    T performInstruction(String paragraphName, String alternativeParagraphName, String booleanValueSymbol, boolean isLoop, boolean isContraCondition);

    @Override
    String[] tokens();

    @Override
    default T makeInstruction() {
        var tokens = tokens();
        if (!tokens[0].equals("PERFORM")) return null;
        assert tokens.length >= 2;

        abstract class LocallyBased implements PerformInstructionMk<T> {
            @Override
            public final String[] tokens() {
                return tokens;
            }

            @Override
            public abstract T makeInstruction();

            @Override
            public final T performInstruction(String paragraphName, String alternativeParagraphName, String booleanValueSymbol, boolean isLoop, boolean isContraCondition) {
                return PerformInstructionMk.this.performInstruction(paragraphName, alternativeParagraphName, booleanValueSymbol, isLoop, isContraCondition);
            }

            protected String paragraphNameOf() {
                return tokens[1];
            }
        }

        var instruction = switch (tokens.length) {
            case 2 -> new LocallyBased() {
                @Override
                public T makeInstruction() {
                    return performInstruction(paragraphNameOf(), null, null, false, false);
                }
            };
            case 4 -> switch (tokens[2]) {
                case "IF" -> new LocallyBased() {
                    @Override
                    public T makeInstruction() {
                        return performInstruction(paragraphNameOf(), null, tokens[3], false, false);
                    }
                };
                case "WHILE" -> new LocallyBased() {
                    @Override
                    public T makeInstruction() {
                        return performInstruction(paragraphNameOf(), null, tokens[3], true, false);
                    }
                };
                default -> throw UNRECOGNIZED_PERFORM_DECORATOR.error(tokens[2]);
            };
            case 5 -> (!"IF".equals(tokens[2]) || !"NOT".equals(tokens[3])) ? null : new LocallyBased() {
                @Override
                public T makeInstruction() {
                    return performInstruction(paragraphNameOf(), null, tokens[4], false, true);
                }
            };
            case 6 -> (!"IF".equals(tokens[2]) || !"ELSE".equals(tokens[4])) ? null : new LocallyBased() {
                @Override
                public T makeInstruction() {
                    return performInstruction(paragraphNameOf(), tokens[5], tokens[3], false, false);
                }
            };
            default -> null;
        };

        if (instruction == null) throw MALFORMED_PERFORM_INSTRUCTION.error(Arrays.toString(tokens));
        return instruction.makeInstruction();
    }

}
