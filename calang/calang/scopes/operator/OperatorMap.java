package calang.scopes.operator;

import calang.rejections.Rejections;
import calang.types.TypedValue;
import calang.types.Operator;

public interface OperatorMap {

    <T extends TypedValue<T>> Operator<T> maybeOperator(Class<T> typedValue, String operatorName);

    default <T extends TypedValue<T>> Operator<T> operatorOrDie(Class<T> typedValue, String operatorName) {
        Operator<T> op = maybeOperator(typedValue, operatorName);
        if (op == null)
            throw Rejections.UNSUPPORTED_OPERATOR.error(operatorName, typedValue.getSimpleName());
        return op;
    }

}
