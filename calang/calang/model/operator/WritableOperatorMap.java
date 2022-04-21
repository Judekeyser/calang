package calang.model.operator;

import calang.model.operator.meta.Operators;
import calang.model.types.*;
import calang.model.types.dummy.Dummy;
import calang.rejections.Rejections;

import java.util.*;

public interface WritableOperatorMap extends OperatorMap, OperatorRegisterer {

    @Override
    <T extends TypedValue<T>> Operator<T> operatorForName(Class<T> typedValue, String operatorName);

    @Override
    <T extends TypedValue<T>> void registerOperator(Class<T> clz, String operatorName, Operator<T> operator);

    static WritableOperatorMap getMutableDefaultOperatorMap() {
        return new WritableOperatorMap() {
            private final Map<Class<? extends TypedValue<?>>, Map<String, ? extends Operator<?>>> map = new HashMap<>();
            {
                for (var knownValue : WritableTypeMap.getMutableDefaultTypeMap().knownValues()) {
                    @SuppressWarnings("unchecked") // We need a T extends TypedValue<T> so we please the compiler
                    var value = (Class<Dummy>) knownValue;

                    registerOperatorsOf(value);
                }
            }

            @Override
            public <T extends TypedValue<T>> Operator<T> operatorForName(Class<T> typedValue, String operatorName) {
                Operator<T> op = get(safeGet(typedValue), operatorName);
                if (op == null)
                    throw Rejections.UNSUPPORTED_OPERATOR.error(operatorName, typedValue.getSimpleName());
                return op;
            }

            @Override
            public <T extends TypedValue<T>> void registerOperator(Class<T> clz, String operatorName, Operator<T> operator) {
                if (map.containsKey(clz)) {
                    assert safeGet(clz) != null;
                    safeGet(clz).put(operatorName, operator);
                } else {
                    map.put(clz, new HashMap<>());
                    registerOperator(clz, operatorName, operator);
                }
            }

            private <T extends TypedValue<T>> Operator<T> get(Map<String, ? extends Operator<T>> ops, String operatorName) {
                return ops == null ? null : ops.get(operatorName);
            }

            @SuppressWarnings("unchecked")
            private <T extends TypedValue<T>> Map<String, Operator<T>> safeGet(Class<T> typedValue) {
                return (Map<String, Operator<T>>) map.get(typedValue);
            }

            private <T extends TypedValue<T>> void registerOperatorsOf(Class<T> clz) {
                var operators = clz.getAnnotationsByType(Operators.class)[0].value();
                for (var operator : operators) {
                    @SuppressWarnings("unchecked") // We need a <T extends TypedValue<T>> , so we mute type-checking
                    var returnType = (Class<Dummy>) operator.returnType();
                    Operator<T> op;
                    if(operator.variadicArgument() == Dummy.class) {
                        op = calang.model.operator.Operators.operatorOf(clz, returnType,
                                new ArrayList<>(Arrays.asList(operator.arguments())));
                    } else {
                        assert operator.arguments().length == 0
                                : "You should not set both a variadic argument and standard arguments";
                        op = calang.model.operator.Operators.operatorOf(clz, returnType,
                                operator.variadicArgument()
                        );
                    }
                    registerOperator(clz, operator.name(), op);
                }
            }
        };
    }
}
