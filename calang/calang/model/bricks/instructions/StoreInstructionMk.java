package calang.model.bricks.instructions;

import java.util.Arrays;
import java.util.stream.Collectors;

import static calang.rejections.Rejections.MALFORMED_STORE_INSTRUCTION;

public interface StoreInstructionMk<T> extends InstructionMk<T>, TokensBased {

    @Override
    String[] tokens();

    T storeInstruction(String targetSymbol, String sourceSymbol);

    @Override
    default T makeInstruction() {
        var tokens = tokens();
        if(! tokens[0].equals("STORE")) return null;

        if (tokens.length < 3 || !"IN".equals(tokens[1]))
            throw MALFORMED_STORE_INSTRUCTION.error(Arrays.toString(tokens));

        var targetSymbol = tokens[2];
        return storeInstruction(
                targetSymbol,
                Arrays.stream(tokens).skip(3).collect(Collectors.joining(" "))
        );
    }
}
