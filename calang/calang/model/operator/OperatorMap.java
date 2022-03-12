package calang.model.operator;

import calang.model.TypedValue;
import calang.model.Operator;

public interface OperatorMap {

    <T extends TypedValue<T>> Operator<T> operatorForName(Class<T> typedValue, String operatorName);

}
