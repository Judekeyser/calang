package calang.model.types;

import calang.model.operator.Operator;
import calang.model.operator.Operators;

import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class IntegerValue implements TypedValue<IntegerValue> {

    /**
     * Default operators on the IntegerValue types
     */

    public static final Map<String, Operator<IntegerValue>> DEFAULT_OPERATORS = Map.of(
        "NEQ", Operators.operatorOf(IntegerValue.class, BooleanValue.class, singletonList(IntegerValue.class)),
        "PREC", Operators.operatorOf(IntegerValue.class, IntegerValue.class, emptyList()),
        "SUCC", Operators.operatorOf(IntegerValue.class, IntegerValue.class, emptyList())
    );

}
