package calang.model.verifier;

import calang.model.bricks.instructions.ComptInstructionMk;
import calang.model.operator.OperatorMap;
import calang.model.bricks.Scope;
import calang.model.types.dummy.Dummy;

import java.util.Arrays;
import java.util.List;

import static calang.rejections.Rejections.UNAPPLICABLE_OPERATOR;
import static calang.rejections.Rejections.UNSTORABLE_OPERATOR_RESULT;

public interface VerifiedComptInstruction<T> extends ComptInstructionMk<T> {

    ComptInstructionMk<T> baseInstruction();

    Scope scope();

    OperatorMap operators();

    @Override
    default T computeInstruction(String targetSymbol, String baseSymbol, String operatorName, List<String> parameterSymbols) {
        class Helper {
            Class<Dummy> hackSymbol(String token) {
                return scope().typeOf(token);
            }
        } var helper = new Helper();
        var base = helper.hackSymbol(baseSymbol);
        var target = helper.hackSymbol(targetSymbol);
        var parameters = parameterSymbols.stream().map(helper::hackSymbol).toList();
        var operator = operators().operatorForName(base, operatorName);

        if (! operator.canBeStoredIn(target))
            throw UNSTORABLE_OPERATOR_RESULT.error(operatorName, base.getSimpleName(), target.getSimpleName());
        if (! operator.doesAccept(parameters))
            throw UNAPPLICABLE_OPERATOR.error(operator, base.getSimpleName(), Arrays.toString(parameters.stream().map(Class::getSimpleName).toArray()));

        return baseInstruction().computeInstruction(targetSymbol, baseSymbol, operatorName, parameterSymbols);
    }

}
