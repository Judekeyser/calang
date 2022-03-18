package calang.model.types;

public interface TypeRegisterer {

    <T extends TypedValue<T>> void registerType(String typeSymbol, Class<T> type);

}
