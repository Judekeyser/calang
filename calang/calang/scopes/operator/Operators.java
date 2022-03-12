package calang.scopes.operator;

import calang.types.TypedValue;
import calang.types.Operator;

import java.util.List;

public class Operators {

    private static abstract class WithReturnType<T extends TypedValue<T>> implements Operator<T> {
        private final Class<?> returnType;
        WithReturnType(Class<?> returnType) {
            this.returnType = returnType;
        }

        @Override
        public <S extends TypedValue<S>> boolean canBeStoredIn(Class<S> store) {
            return returnType == store;
        }
    }

    static <T extends TypedValue<T>, R extends TypedValue<R>> Operator<T> describes(
            Class<T> baseType, Class<R> returnType,
            List<Class<? extends TypedValue<?>>> typeChecker
    ) {
        class Impl extends WithReturnType<T> {
            Impl() { super(returnType); }

            @Override
            public boolean doesAccept(List<? extends Class<? extends TypedValue<?>>> clz) {
                check: if(clz.size() == typeChecker.size()) {
                    for (int i = 0; i < clz.size(); i++)
                        if (clz.get(i) != typeChecker.get(i)) break check;
                    return true;
                } return false;
            }
        } return new Impl();
    }

    static <T extends TypedValue<T>, R extends TypedValue<R>> Operator<T> describes(
            Class<T> baseType, Class<R> returnType, Class<? extends TypedValue<?>> typeChecker
    ) {
        class Impl extends WithReturnType<T> {
            Impl() { super(returnType); }

            @Override
            public boolean doesAccept(List<? extends Class<? extends TypedValue<?>>> clz) {
                for (Class<? extends TypedValue<?>> aClass : clz)
                    if (aClass != typeChecker)
                        return false;
                return true;
            }
        } return new Impl();
    }

}
