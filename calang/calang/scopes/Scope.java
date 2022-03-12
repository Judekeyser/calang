package calang.scopes;

import calang.scopes.operator.OperatorMap;
import calang.types.TypedValue;

import java.util.Arrays;
import java.util.List;

import static calang.rejections.Rejections.*;

public interface Scope extends OperatorMap {

    List<String> symbolList();

    List<String> inputsList();

    List<String> outputsList();

    <T extends TypedValue<T>> Class<T> typeOf(String token);

    default void assertOperatorUsageValid(String baseSymbol, String targetSymbol, String operatorName, List<String> parameterSymbols) {
        class AnonymousMagnet implements TypedValue<AnonymousMagnet> {
            Class<AnonymousMagnet> hackSymbol(String token) {
                return Scope.this.typeOf(token);
            }
        } var helper = new AnonymousMagnet();
        var base = helper.hackSymbol(baseSymbol);
        var target = helper.hackSymbol(targetSymbol);
        var parameters = parameterSymbols.stream().map(helper::hackSymbol).toList();
        var operator = operatorOrDie(base, operatorName);

        if (! operator.canBeStoredIn(target))
            throw UNSTORABLE_OPERATOR_RESULT.error(operatorName, base.getSimpleName(), target.getSimpleName());
        if (! operator.doesAccept(parameters))
            throw UNAPPLICABLE_OPERATOR.error(operator, base.getSimpleName(), Arrays.toString(parameters.stream().map(Class::getSimpleName).toArray()));
    }

    default boolean isSymbolDeclared(String token) {
        return symbolList().contains(token);
    }
}
