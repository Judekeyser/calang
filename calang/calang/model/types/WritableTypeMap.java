package calang.model.types;

import java.util.HashMap;

import static calang.rejections.Rejections.UNSUPPORTED_TYPE;

public interface WritableTypeMap extends TypeMap, TypeRegisterer {

    @Override
    <T extends TypedValue<T>> Class<T> typeForSymbol(String typeSymbol);

    @Override
    <T extends TypedValue<T>> void registerType(String typeSymbol, Class<T> type);

    static WritableTypeMap getMutableDefaultTypeMap() {
        return new WritableTypeMap() {
            private final HashMap<String, Class<?>> map = new HashMap<>() {{
                put("INTEGER", IntegerValue.class);
                put("BYTES", BytesValue.class);
                put("BOOLEAN", BooleanValue.class);
                put("PROGRAM", ProgramValue.class);
            }};

            @Override
            @SuppressWarnings("unchecked")
            public <T extends TypedValue<T>> Class<T> typeForSymbol(String typeSymbol) {
                var type = map.get(typeSymbol);
                if (type == null) throw UNSUPPORTED_TYPE.error(typeSymbol);
                return (Class<T>) type;
            }

            @Override
            public <T extends TypedValue<T>> void registerType(String typeSymbol, Class<T> type) {
                map.put(typeSymbol, type);
            }
        };
    }

}
