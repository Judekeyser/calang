package calang.model.operator;

import calang.model.types.*;
import calang.rejections.Rejections;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

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
                registerOperator(IntegerValue.class, "NEQ", BooleanValue.class, singletonList(IntegerValue.class));
                registerOperator(IntegerValue.class, "PREC", IntegerValue.class, emptyList());
                registerOperator(IntegerValue.class, "SUCC", IntegerValue.class, emptyList());
                registerOperator(BytesValue.class, "|.|", IntegerValue.class, emptyList());
                registerOperator(BooleanValue.class, "NEGATE", BooleanValue.class, emptyList());
                registerOperator(BooleanValue.class, "AND", BooleanValue.class, BooleanValue.class);
                registerOperator(BooleanValue.class, "OR", BooleanValue.class, BooleanValue.class);
                registerOperator(BooleanValue.class, "XAND", BooleanValue.class, BooleanValue.class);
                registerOperator(BooleanValue.class, "XOR", BooleanValue.class, BooleanValue.class);
                registerOperator(BooleanValue.class, "IMPLIES", BooleanValue.class, singletonList(IntegerValue.class));
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
