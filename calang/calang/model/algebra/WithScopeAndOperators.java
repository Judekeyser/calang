package calang.model.algebra;

import calang.model.Scope;
import calang.model.operator.OperatorMap;
import calang.types.dummy.Dummy;

import java.util.Arrays;
import java.util.List;

import static calang.rejections.Rejections.UNAPPLICABLE_OPERATOR;
import static calang.rejections.Rejections.UNSTORABLE_OPERATOR_RESULT;

public interface WithScopeAndOperators {

    Scope scope();

    OperatorMap operators();

    default void assertOperatorUsageValid(String baseSymbol, String targetSymbol, String operatorName, List<String> parameterSymbols) {
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
    }

}
