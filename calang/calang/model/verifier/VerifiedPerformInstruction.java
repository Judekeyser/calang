package calang.model.verifier;

import calang.model.bricks.instructions.PerformInstructionMk;
import calang.model.bricks.Scope;
import calang.rejections.Rejections;
import calang.model.types.BooleanValue;

public interface VerifiedPerformInstruction extends PerformInstructionMk<Void> {

    boolean isParagraphUnknown(String pargraphName);

    Scope scope();

    @Override
    default Void performInstruction(String paragraphName, String alternativeParagraphName, String booleanValueSymbol, boolean isLoop, boolean isContraCondition) {
        if(isParagraphUnknown(paragraphName))
            throw Rejections.UNDEFINED_PARAGRAPH.error(paragraphName);
        if(alternativeParagraphName != null && isParagraphUnknown(alternativeParagraphName))
            throw Rejections.UNDEFINED_PARAGRAPH.error(alternativeParagraphName);
        var flagTypeIfAny = booleanValueSymbol == null ? null:
                scope().typeOf(booleanValueSymbol);
        if(flagTypeIfAny != null && ! flagTypeIfAny.equals(BooleanValue.class))
            throw Rejections.BOOLEAN_FLAG_IS_NOT_BOOLEAN.error(booleanValueSymbol, flagTypeIfAny.getSimpleName());

        return null;
    }

}
