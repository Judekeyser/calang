package calang.model.types;

import calang.model.types.meta.TypeName;

import java.util.HashMap;

import static calang.rejections.Rejections.UNSUPPORTED_TYPE;

public interface WritableTypeMap extends TypeMap, TypeRegisterer {

    @Override
    <T extends TypedValue<T>> Class<T> typeForSymbol(String typeSymbol);

    @Override
    <T extends TypedValue<T>> void registerType(String typeSymbol, Class<T> type);

    Iterable<Class<? extends TypedValue<?>>> knownValues();

    static WritableTypeMap getMutableDefaultTypeMap() {
        return new WritableTypeMap() {
            private final HashMap<String, Class<? extends TypedValue<?>>> map = new HashMap<>();
            {
                registerTypeOf(IntegerValue.class);
                registerTypeOf(BytesValue.class);
                registerTypeOf(BooleanValue.class);
                registerTypeOf(ProgramValue.class);
            }

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

            @Override
            public Iterable<Class<? extends TypedValue<?>>> knownValues() {
                return map.values();
            }

            private <T extends TypedValue<T>> void registerTypeOf(Class<T> clz) {
                var typeName = clz.getAnnotationsByType(TypeName.class)[0].value();
                registerType(typeName, clz);
            }
        };
    }

}
