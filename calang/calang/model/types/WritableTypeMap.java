package calang.model.types;

import calang.model.TypedValue;

import java.util.HashMap;

import static calang.rejections.Rejections.UNSUPPORTED_TYPE;

public interface WritableTypeMap extends TypeMap, TypeRegisterer {

    @Override
    <T extends TypedValue<T>> Class<T> typeForSymbol(String typeSymbol);

    @Override
    <T extends TypedValue<T>> void registerType(String typeSymbol, Class<T> type);

    static WritableTypeMap newWritableTypeMap() {
        return new WritableTypeMap() {
            private final HashMap<String, Class<?>> map = new HashMap<>();

            @Override
            @SuppressWarnings("unchecked")
            public <T extends TypedValue<T>> Class<T> typeForSymbol(String typeSymbol) {
                var type = map.get(typeSymbol);
                if(type == null)
                    throw UNSUPPORTED_TYPE.error(typeSymbol);
                return (Class<T>) type;
            }

            @Override
            public <T extends TypedValue<T>> void registerType(String typeSymbol, Class<T> type) {
                map.put(typeSymbol, type);
            }
        };
    }

}
