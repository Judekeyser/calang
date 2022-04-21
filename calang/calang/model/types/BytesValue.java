package calang.model.types;

import calang.model.operator.meta.Operator;
import calang.model.operator.meta.Operators;

@Operators({
        @Operator(
                name = "|.|",
                returnType = IntegerValue.class
        )
})
public class BytesValue implements TypedValue<BytesValue> {}
