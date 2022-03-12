package calang.scopes;

import calang.types.TypedValue;

import java.util.List;

public interface Operator<T extends TypedValue<T>> {

    boolean doesAccept(List<? extends Class<? extends TypedValue<?>>> clz);

    <S extends TypedValue<S>> boolean canBeStoredIn(Class<S> store);

}
