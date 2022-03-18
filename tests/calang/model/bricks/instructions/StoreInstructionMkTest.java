package calang.model.bricks.instructions;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class StoreInstructionMkTest extends InstructionMkTestTemplate {

    @Test
    public void makeInstruction_shouldReturnsOk_givenSomeRightHand() {
        class Spy implements StoreInstructionMk<Object[]> {
            @Override
            public Object[] storeInstruction(String targetSymbol, String sourceSymbol) {
                return new Object[] { targetSymbol, sourceSymbol };
            }

            @Override
            public String[] tokens() {
                return tokensOf("STORE IN $X Something  funny");
            }
        }
        assertArrayEquals(new Object[] { "$X", "Something funny" },
                new Spy().makeInstruction());
    }

}
