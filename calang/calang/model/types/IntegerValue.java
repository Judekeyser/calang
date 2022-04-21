package calang.model.types;

import calang.model.operator.meta.Operator;
import calang.model.operator.meta.Operators;
import calang.model.types.meta.TypeName;

@Operators({
        @Operator(name = "NEQ", returnType = BooleanValue.class, arguments = {IntegerValue.class}),
        @Operator(name = "PREC", returnType = IntegerValue.class),
        @Operator(name = "SUCC", returnType = IntegerValue.class)
})
@TypeName("INTEGER")
public class IntegerValue implements TypedValue<IntegerValue> {}
