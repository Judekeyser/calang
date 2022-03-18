package calang.model.bricks.instructions;

import org.junit.Test;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.junit.Assert.assertArrayEquals;

public class ComptInstructionMkTest extends InstructionMkTestTemplate {

    @Test
    public void makeInstruction_shouldReturnsOk_givenSomeRightHand() {
        assertOk("COMPT IN $Y $X + $A $B $C",
                "$Y", "$X", "+", List.of("$A", "$B", "$C")
        );
        assertOk("COMPT IN $Y $X ++",
                "$Y", "$X", "++", emptyList()
        );
    }

    void assertOk(String instruction, Object... array) {
        class Spy implements ComptInstructionMk<Object[]> {
            @Override
            public Object[] computeInstruction(String targetSymbol, String baseSymbol, String operator, List<String> parameterSymbols) {
                return new Object[] {
                        targetSymbol, baseSymbol, operator,
                        parameterSymbols
                };
            }

            @Override
            public String[] tokens() {
                return tokensOf(instruction);
            }
        }
        assertArrayEquals(array, new Spy().makeInstruction());
    }

}
