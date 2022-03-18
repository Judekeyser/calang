package calang.model.operator;

import calang.model.types.TypedValue;

public interface OperatorMap {

    <T extends TypedValue<T>> Operator<T> operatorForName(Class<T> typedValue, String operatorName);

}
