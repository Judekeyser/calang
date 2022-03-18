package calang.model.bricks.instructions;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class PrintInstructionMkTest extends InstructionMkTestTemplate {

    @Test
    public void makeInstruction_shouldReturnsOk_givenTokenList() {
        class Spy implements PrintInstructionMk<List<String>> {
            @Override
            public List<String> printInstruction(List<String> tokens) {
                return tokens;
            }

            @Override
            public String[] tokens() {
                return tokensOf("PRINT HELLO WORLD");
            }
        }
        assertEquals(List.of("HELLO", "WORLD"),
                new Spy().makeInstruction());
    }

}
