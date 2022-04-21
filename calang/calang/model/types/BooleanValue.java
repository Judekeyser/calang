package calang.model.types;

import calang.model.operator.Operator;
import calang.model.operator.Operators;

import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class BooleanValue implements TypedValue<BooleanValue> {

    /**
     * Default operators on the BooleanValue types
     */

    public static final Map<String, Operator<BooleanValue>> DEFAULT_OPERATORS = Map.of(
        "NEGATE", Operators.operatorOf(BooleanValue.class, BooleanValue.class, emptyList()),
        "AND", Operators.operatorOf(BooleanValue.class, BooleanValue.class, BooleanValue.class),
        "OR", Operators.operatorOf(BooleanValue.class, BooleanValue.class, BooleanValue.class),
        "XAND", Operators.operatorOf(BooleanValue.class, BooleanValue.class, BooleanValue.class),
        "XOR", Operators.operatorOf(BooleanValue.class, BooleanValue.class, BooleanValue.class),
        "IMPLIES", Operators.operatorOf(BooleanValue.class, BooleanValue.class, singletonList(IntegerValue.class))
    );

}
