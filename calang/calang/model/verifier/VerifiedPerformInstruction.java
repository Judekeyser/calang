package calang.model.verifier;

import calang.model.bricks.Paragraphs;
import calang.model.bricks.instructions.PerformInstructionMk;
import calang.model.bricks.Scope;
import calang.rejections.Rejections;
import calang.model.types.BooleanValue;

import java.util.function.Predicate;

import static java.util.function.Predicate.not;

public interface VerifiedPerformInstruction<T> extends PerformInstructionMk<T> {

    PerformInstructionMk<T> baseInstruction();

    Paragraphs paragraphs();

    Scope scope();

    @Override
    default T performInstruction(String paragraphName, String alternativeParagraphName, String booleanValueSymbol, boolean isLoop, boolean isContraCondition) {
        Predicate<String> absentParagraph = not(paragraphs().paragraphsByName()::containsKey);

        if(absentParagraph.test(paragraphName))
            throw Rejections.UNDEFINED_PARAGRAPH.error(paragraphName);
        if(alternativeParagraphName != null && absentParagraph.test(alternativeParagraphName))
            throw Rejections.UNDEFINED_PARAGRAPH.error(alternativeParagraphName);
        var flagTypeIfAny = booleanValueSymbol == null ? null:
                scope().typeOf(booleanValueSymbol);
        if(flagTypeIfAny != null && ! flagTypeIfAny.equals(BooleanValue.class))
            throw Rejections.BOOLEAN_FLAG_IS_NOT_BOOLEAN.error(booleanValueSymbol, flagTypeIfAny.getSimpleName());

        return baseInstruction().performInstruction(paragraphName, alternativeParagraphName, booleanValueSymbol, isLoop, isContraCondition);
    }

}
