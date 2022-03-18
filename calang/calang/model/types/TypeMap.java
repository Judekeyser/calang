package calang.model.types;

public interface TypeMap {

    <T extends TypedValue<T>> Class<T> typeForSymbol(String typeSymbol);

}
