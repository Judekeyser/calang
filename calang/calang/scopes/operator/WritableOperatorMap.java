package calang.scopes.operator;

import calang.types.Operator;
import calang.types.TypedValue;

import java.util.HashMap;
import java.util.Map;

public interface WritableOperatorMap extends OperatorMap, OperatorRegisterer {

    @Override
    <T extends TypedValue<T>> Operator<T> maybeOperator(Class<T> typedValue, String operatorName);

    @Override
    <T extends TypedValue<T>> void addOperator(Class<T> clz, String operatorName, Operator<T> operator);

    static WritableOperatorMap ofMap() {
        return new WritableOperatorMap() {
            private final Map<Class<? extends TypedValue<?>>, Map<String, ? extends Operator<?>>> map = new HashMap<>();

            @Override
            public <T extends TypedValue<T>> Operator<T> maybeOperator(Class<T> typedValue, String operatorName) {
                return get(safeGet(typedValue), operatorName);
            }

            @Override
            public <T extends TypedValue<T>> void addOperator(Class<T> clz, String operatorName, Operator<T> operator) {
                if (map.containsKey(clz)) {
                    assert safeGet(clz) != null;
                    safeGet(clz).put(operatorName, operator);
                } else {
                    map.put(clz, new HashMap<>());
                    addOperator(clz, operatorName, operator);
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
