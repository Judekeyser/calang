package calang.model.bricks.instructions;

import java.util.Arrays;
import java.util.List;

import static calang.rejections.Rejections.MALFORMED_COMPT_INSTRUCTION;

public interface ComptInstructionMk<T> extends InstructionMk<T>, TokensBased {

    T computeInstruction(
            String targetSymbol,
            String baseSymbol,
            String operator,
            List<String> parameterSymbols
    );

    @Override
    String[] tokens();

    @Override
    default T makeInstruction() {
        var tokens = tokens();
        if(! tokens[0].equals("COMPT")) return null;

        if (tokens.length < 5 || !"IN".equals(tokens[1]))
            throw MALFORMED_COMPT_INSTRUCTION.error(Arrays.toString(tokens));

        var targetSymbol = tokens[2];
        var baseSymbol = tokens[3];
        var operator = tokens[4];
        return computeInstruction(
                targetSymbol,
                baseSymbol,
                operator,
                Arrays.stream(tokens).skip(5).toList()
        );
    }
}
