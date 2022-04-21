package calang.model.operator;

import calang.model.types.*;
import calang.rejections.Rejections;

import java.util.HashMap;
import java.util.Map;

public interface WritableOperatorMap extends OperatorMap, OperatorRegisterer {

    @Override
    <T extends TypedValue<T>> Operator<T> operatorForName(Class<T> typedValue, String operatorName);

    @Override
    <T extends TypedValue<T>> void registerOperator(Class<T> clz, String operatorName, Operator<T> operator);

    static WritableOperatorMap getMutableDefaultOperatorMap() {
        return new WritableOperatorMap() {
            private final Map<Class<? extends TypedValue<?>>, Map<String, ? extends Operator<?>>> map = new HashMap<>() {{
                put(IntegerValue.class, new HashMap<>());
                put(BytesValue.class, new HashMap<>());
                put(BooleanValue.class, new HashMap<>());
                put(ProgramValue.class, new HashMap<>());
            }};
            {
                for (var e : IntegerValue.DEFAULT_OPERATORS.entrySet())
                    registerOperator(IntegerValue.class, e.getKey(), e.getValue());

                for (var e : BytesValue.DEFAULT_OPERATORS.entrySet())
                    registerOperator(BytesValue.class, e.getKey(), e.getValue());

                for (var e : BooleanValue.DEFAULT_OPERATORS.entrySet())
                    registerOperator(BooleanValue.class, e.getKey(), e.getValue());
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
        };
    }
}
