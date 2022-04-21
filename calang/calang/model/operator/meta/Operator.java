package calang.model.operator.meta;

import calang.model.types.TypedValue;
import calang.model.types.dummy.Dummy;

public @interface Operator {

    String name();

    Class<? extends TypedValue<?>> returnType();

    Class<? extends TypedValue<?>>[] arguments() default {};

    Class<? extends TypedValue<?>> variadicArgument() default Dummy.class;

}
