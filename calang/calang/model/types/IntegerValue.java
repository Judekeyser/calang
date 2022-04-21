package calang.model.types;

import calang.model.operator.meta.Operator;
import calang.model.operator.meta.Operators;

@Operators({
        @Operator(name = "NEQ", returnType = BooleanValue.class, arguments = {IntegerValue.class}),
        @Operator(name = "PREC", returnType = IntegerValue.class),
        @Operator(name = "SUCC", returnType = IntegerValue.class)
})
public class IntegerValue implements TypedValue<IntegerValue> {}
