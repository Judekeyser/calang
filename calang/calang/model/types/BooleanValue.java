package calang.model.types;

import calang.model.operator.meta.Operator;
import calang.model.operator.meta.Operators;

@Operators({
        @Operator(
                name = "NEGATE",
                returnType = BooleanValue.class
        ),
        @Operator(
                name = "AND",
                returnType = BooleanValue.class,
                variadicArgument = BooleanValue.class
        ),
        @Operator(
                name = "OR",
                returnType = BooleanValue.class,
                variadicArgument = BooleanValue.class
        ),
        @Operator(
                name = "XAND",
                returnType = BooleanValue.class,
                variadicArgument = BooleanValue.class
        ),
        @Operator(
                name = "XOR",
                returnType = BooleanValue.class,
                variadicArgument = BooleanValue.class
        ),
        @Operator(
                name = "IMPLIES",
                returnType = BooleanValue.class,
                arguments = { BooleanValue.class }
        )
})
public class BooleanValue implements TypedValue<BooleanValue> {}
