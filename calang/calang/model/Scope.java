package calang.model;

import calang.rejections.Rejections;

import java.util.Set;
import java.util.stream.Collectors;

public interface Scope {

    record Declaration<T extends TypedValue<T>> (String identifier, Class<T> type) {}

    Set<Declaration<?>> declarations();

    Set<Declaration<?>> inputDeclarations();

    Set<Declaration<?>> outputDeclarations();

    default Set<String> symbols() {
        return declarations().stream().map(Declaration::identifier).collect(Collectors.toSet());
    }

    default Set<String> inputSymbols() {
        return inputDeclarations().stream().map(Declaration::identifier).collect(Collectors.toSet());
    }

    default Set<String> outputSymbols() {
        return outputDeclarations().stream().map(Declaration::identifier).collect(Collectors.toSet());
    }

    @SuppressWarnings("unchecked")
    default <T extends TypedValue<T>> Class<T> typeOf(String token) {
        var declaration = declarations().stream()
                .filter(d -> d.identifier.equals(token))
                .findAny().orElseThrow(() -> Rejections.UNKNOWN_VARIABLE.error(token));
        assert declaration.identifier.equals(token);
        return (Class<T>) declaration.type();
    }

    default boolean isSymbolDeclared(String token) {
        return declarations().stream().anyMatch(d -> d.identifier.equals(token));
    }
}
