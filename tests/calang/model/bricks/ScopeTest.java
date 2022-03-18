package calang.model.bricks;

import calang.model.types.*;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * This test class is related to testing the parser in a end-to-end manner.
 */
public class ScopeTest {

    @Test
    public void scope_shouldParseCorrect_prog() {
        var scope = scope("""
                DECLARE  $MESSAGE BYTES
                DECLARE  $LENGTH INTEGER

                BEGIN.
                  CALL password_input $MESSAGE << $TEXT_RECORD
                  CALL bytelength $MESSAGE >> $CHUNK $LENGTH << $LENGTH
                  CALL tower $LENGTH >> $HEIGHT
                """);

        assertTrue(scope.outputs().isEmpty());
        assertTrue(scope.inputs().isEmpty());

        assertSame(BytesValue.class, scope.typeOf("$MESSAGE"));
        assertSame(IntegerValue.class, scope.typeOf("$LENGTH"));
    }

    @Test
    public void scope_shouldParseCorrect_byteslength() {
        var scope = scope("""
                DECLARE INPUT $CHUNK BYTES
                DECLARE OUTPUT $LENGTH INTEGER
                
                START.
                  COMPT IN $LENGTH $CHUNK |.|
                """);

        assertTrue(scope.inputs().contains(new Scope.Declaration<>("$CHUNK", BytesValue.class, Scope.DeclarationModifier.INPUT)));
        assertTrue(scope.outputs().contains(new Scope.Declaration<>("$LENGTH", IntegerValue.class, Scope.DeclarationModifier.OUTPUT)));

        assertSame(BytesValue.class, scope.typeOf("$CHUNK"));
        assertSame(IntegerValue.class, scope.typeOf("$LENGTH"));
    }

    @Test
    public void scope_shouldParseCorrect_tower() {
        var scope = scope("""
                DECLARE INPUT $HEIGHT INTEGER
                DECLARE  $LOCAL_HEIGHT INTEGER
                DECLARE  $CURSOR INTEGER
                DECLARE  $FLAG BOOLEAN

                MAIN.
                  STORE IN $LOCAL_HEIGHT 1
                  STORE IN $FLAG $HEIGHT
                  PERFORM PRINT_LINE WHILE $FLAG

                PRINT_LINE.
                  STORE IN $CURSOR 1
                  STORE IN $FLAG 1
                  PERFORM PRINT_COLUMN WHILE $FLAG
                  PRINT \\n
                  COMPT IN $FLAG $HEIGHT NEQ $LOCAL_HEIGHT
                  COMPT IN $LOCAL_HEIGHT $LOCAL_HEIGHT SUCC

                PRINT_COLUMN.
                  PRINT #
                  COMPT IN $FLAG $LOCAL_HEIGHT NEQ $CURSOR
                  COMPT IN $CURSOR $CURSOR SUCC
                """);

        assertTrue(scope.inputs().contains(new Scope.Declaration<>("$HEIGHT", IntegerValue.class, Scope.DeclarationModifier.INPUT)));
        assertTrue(scope.declarations().contains(new Scope.Declaration<>("$CURSOR", IntegerValue.class, null)));
        assertTrue(scope.outputs().isEmpty());

        assertTrue(List.of("$HEIGHT", "$LOCAL_HEIGHT", "$CURSOR", "$FLAG").containsAll(
                scope.declarations().stream().map(Scope.Declaration::name).toList())
        );

        assertSame(IntegerValue.class, scope.typeOf("$HEIGHT"));
        assertSame(IntegerValue.class, scope.typeOf("$LOCAL_HEIGHT"));
        assertSame(IntegerValue.class, scope.typeOf("$CURSOR"));
        assertSame(BooleanValue.class, scope.typeOf("$FLAG"));
    }

    static Scope scope(String input) {
        var lines = input.lines().toList();
        var defaultTypeMap = WritableTypeMap.getMutableDefaultTypeMap();
        return new Scope() {
            @Override
            public List<String> programSpecification() {
                return lines;
            }

            @Override
            public TypeMap typeMap() {
                return defaultTypeMap;
            }
        };
    }

}
