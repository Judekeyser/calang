package calang.model.types;

import calang.model.TypedValue;

public interface TypeMap {

    <T extends TypedValue<T>> Class<T> typeForSymbol(String typeSymbol);

}
