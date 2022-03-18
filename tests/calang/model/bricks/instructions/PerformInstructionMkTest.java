package calang.model.bricks.instructions;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class PerformInstructionMkTest extends InstructionMkTestTemplate {

    @Test
    public void makeInstruction_shouldReturnsOk_givenSimplePerform() {
        assertOk("PERFORM PARA",
                "PARA", null, null, false, false
        );
    }

    @Test
    public void makeInstruction_shouldReturnsOk_givenSimpleIfPerform() {
        assertOk("PERFORM PARA IF $STUFF",
                "PARA", null, "$STUFF", false, false
        );
    }

    @Test
    public void makeInstruction_shouldReturnsOk_givenSimpleIfElsePerform() {
        assertOk("PERFORM PARA IF $STUFF ELSE ROLLBACK",
                "PARA", "ROLLBACK", "$STUFF", false, false
        );
    }

    @Test
    public void makeInstruction_shouldReturnsOk_givenSimpleIfNotPerform() {
        assertOk("PERFORM PARA IF NOT $STUFF",
                "PARA", null, "$STUFF", false, true
        );
    }

    @Test
    public void makeInstruction_shouldReturnsOk_givenSimpleWhilePerform() {
        assertOk("PERFORM PARA WHILE $STUFF",
                "PARA", null, "$STUFF", true, false
        );
    }

    private void assertOk(String instruction, Object... results) {
        class Spy implements PerformInstructionMk<Object[]> {
            @Override
            public Object[] performInstruction(String paragraphName, String alternativeParagraphName, String booleanValueSymbol, boolean isLoop, boolean isContraCondition) {
                return new Object[]{
                        paragraphName, alternativeParagraphName, booleanValueSymbol, isLoop, isContraCondition
                };
            }

            @Override
            public String[] tokens() {
                return tokensOf(instruction);
            }
        }
        assertArrayEquals(results, new Spy().makeInstruction());
    }

}
