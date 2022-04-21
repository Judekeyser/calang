package calang.model.types;

import calang.model.operator.Operator;
import calang.model.operator.Operators;

import java.util.Map;

import static java.util.Collections.emptyList;

public class BytesValue implements TypedValue<BytesValue> {

    /**
     * Default operators on the BytesValue types
     */

    public static final Map<String, Operator<BytesValue>> DEFAULT_OPERATORS = Map.of(
        "|.|", Operators.operatorOf(BytesValue.class, IntegerValue.class, emptyList())
    );

}
