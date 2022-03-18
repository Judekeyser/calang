package calang.model.bricks.instructions;

import calang.VariableBinding;
import org.junit.Test;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

public class CallInstructionMkTest extends InstructionMkTestTemplate {

    @Test
    public void makeInstruction_shouldReturnsOk_givenNoArgs() {
        assertOk("CALL myProgram",
                "myProgram", emptyList(), emptyList()
        );
    }

    @Test
    public void makeInstruction_shouldReturnsOk_givenInputs() {
        assertOk("CALL myProgram $A >> $X $B >> $Y",
                "myProgram",
                List.of(
                        new VariableBinding("$A", "$X"),
                        new VariableBinding("$B", "$Y")
                ),
                emptyList()
        );
    }

    @Test
    public void makeInstruction_shouldReturnsOk_givenOutputs() {
        assertOk("CALL myProgram $A << $X $B << $Y",
                "myProgram",
                emptyList(),
                List.of(
                        new VariableBinding("$A", "$X"),
                        new VariableBinding("$B", "$Y")
                )
        );
    }

    @Test
    public void makeInstruction_shouldReturnsOk_givenInputsAndOutputs() {
        assertOk("CALL myProgram $A >> $X $B << $Y",
                "myProgram",
                singletonList(new VariableBinding("$A", "$X")),
                singletonList(new VariableBinding("$B", "$Y"))
        );
    }

    @Test
    public void makeInstruction_shouldReturnsOk_givenOutputsAndInputs() {
        assertOk("CALL myProgram $A << $X $B >> $Y",
                "myProgram",
                singletonList(new VariableBinding("$B", "$Y")),
                singletonList(new VariableBinding("$A", "$X"))
        );
    }

    private void assertOk(String instruction, String program, List<VariableBinding> inputs, List<VariableBinding> outputs) {
        class Spy implements CallInstructionMk<Object[]> {
            @Override
            public Object[] callInstruction(String programSymbol, List<VariableBinding> inputs, List<VariableBinding> outputs) {
                return new Object[] {
                        programSymbol,
                        inputs,
                        outputs
                };
            }

            @Override
            public String[] tokens() {
                return tokensOf(instruction);
            }
        }
        var actual = new Spy().makeInstruction();
        assertEquals("Program name mismatch", program, actual[0]);
        assertEquals("Inputs mismatch", inputs, actual[1]);
        assertEquals("Outputs mismatch", outputs, actual[2]);
    }

}
