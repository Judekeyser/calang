package calang.model.types;

import calang.model.operator.meta.Operator;
import calang.model.operator.meta.Operators;
import calang.model.types.meta.TypeName;

@Operators({
        @Operator(
                name = "|.|",
                returnType = IntegerValue.class
        )
})
@TypeName("BYTES")
public class BytesValue implements TypedValue<BytesValue> {}
