package calang.model.operator;

import calang.model.types.TypedValue;

import java.util.List;

public interface OperatorRegisterer {

    <T extends TypedValue<T>> void registerOperator(Class<T> clz, String operatorName, Operator<T> operator);

    default  <T extends TypedValue<T>, R extends TypedValue<R>> void registerOperator(Class<T> clz, String operatorName,
                                                                                      Class<R> returnType,
                                                                                      List<Class<? extends TypedValue<?>>> typeChecker) {
        registerOperator(clz, operatorName, Operators.operatorOf(clz, returnType, typeChecker));
    }
    default <T extends TypedValue<T>, R extends TypedValue<R>> void registerOperator(Class<T> clz, String operatorName,
                                                                                     Class<R> returnType, Class<? extends TypedValue<?>> typeChecker) {
        registerOperator(clz, operatorName, Operators.operatorOf(clz, returnType, typeChecker));
    }

}
