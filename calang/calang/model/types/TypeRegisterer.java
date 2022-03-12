package calang.model.types;

import calang.model.TypedValue;

public interface TypeRegisterer {

    <T extends TypedValue<T>> void registerType(String typeSymbol, Class<T> type);

}
