package calang.scopes.operator;

import calang.types.TypedValue;
import calang.types.Operator;

import java.util.List;

public interface OperatorRegisterer {

    <T extends TypedValue<T>> void addOperator(Class<T> clz, String operatorName, Operator<T> operator);

    default  <T extends TypedValue<T>, R extends TypedValue<R>> void addOperator(Class<T> clz, String operatorName,
                                                                                           Class<R> returnType,
                                                                                           List<Class<? extends TypedValue<?>>> typeChecker) {
        addOperator(clz, operatorName, Operators.describes(clz, returnType, typeChecker));
    }
    default <T extends TypedValue<T>, R extends TypedValue<R>> void addOperator(Class<T> clz, String operatorName,
                                                                                           Class<R> returnType, Class<? extends TypedValue<?>> typeChecker) {
        addOperator(clz, operatorName, Operators.describes(clz, returnType, typeChecker));
    }

}
