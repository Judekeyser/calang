package calang.model.bricks;

import calang.model.types.TypedValue;
import calang.model.types.TypeMap;
import calang.model.types.dummy.Dummy;

import java.util.*;
import java.util.stream.Collectors;

import static calang.rejections.Rejections.MALFORMED_DECLARATION_LINE;
import static calang.rejections.Rejections.UNKNOWN_VARIABLE;
import static java.util.Collections.unmodifiableSet;
import static java.util.Optional.empty;
import static java.util.Optional.of;

public interface Scope {

    List<String> programSpecification();

    TypeMap typeMap();

    default Set<Declaration<?>> declarations() {
        var programSpecification = programSpecification();
        var typeMap = typeMap();
        {
            var declarations = new HashSet<Declaration<?>>();
            for (String line : programSpecification) {
                if (line.isBlank()) continue;
                if (line.startsWith("DECLARE ")) {
                    var tokens = line.trim().split("\s+");
                    assert tokens[0].equals("DECLARE");
                    if (tokens.length < 3) throw MALFORMED_DECLARATION_LINE.error(line);
                    var modifier = tokens.length != 4 ? null : switch (tokens[1]) {
                        case "INPUT" -> DeclarationModifier.INPUT;
                        case "OUTPUT" -> DeclarationModifier.OUTPUT;
                        default -> throw MALFORMED_DECLARATION_LINE.error(line);
                    };
                    var type = typeMap.<Dummy> typeForSymbol(tokens[tokens.length - 1]);
                    declarations.add(new Declaration<>(tokens[tokens.length - 2], type, modifier));
                }
            }
            return unmodifiableSet(declarations);
        }
    }

    default Set<Declaration<?>> inputs() {
        return declarations().stream()
                .filter(DeclarationModifier.INPUT::isMarkerOf)
                .collect(Collectors.toSet());
    }

    default Set<Declaration<?>> outputs() {
        return declarations().stream()
                .filter(DeclarationModifier.OUTPUT::isMarkerOf)
                .collect(Collectors.toSet());
    }

    default <T extends TypedValue<T>> Class<T> typeOf(String name) {
        return this.<T> maybeSymbol(name)
                .orElseThrow(() -> UNKNOWN_VARIABLE.error(name));
    }

    @SuppressWarnings("unchecked")
    default <T extends TypedValue<T>> Optional<Class<T>> maybeSymbol(String name) {
        for (var declaration : declarations()) {
            if(Objects.equals(name, declaration.name()))
                return of((Class<T>) declaration.type());
        }
        return empty();
    }

    enum DeclarationModifier { INPUT, OUTPUT;
        boolean isMarkerOf(Declaration<?> declaration) {
            return equals(declaration.modifier);
        }
    }
    record Declaration<T extends TypedValue<T>>(String name, Class<T> type, DeclarationModifier modifier) {
        @Override
        public boolean equals(Object obj) {
            if(obj == this) return true;
            if(obj instanceof Declaration<?> another) {
                var t = Objects.equals(another.name, name) && Objects.equals(another.type, type);
                if(modifier == null) return t && another.modifier == null;
                else return t && Objects.equals(modifier, another.modifier);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return modifier == null ? Objects.hash(name, type) : Objects.hash(name, type, modifier);
        }
    }

}
