package calang;

import calang.types.BooleanValue;
import calang.types.BytesValue;
import calang.types.IntegerValue;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * This test class is related to testing the parser in a end-to-end manner.
 */
public class CalangParseE2ETest {

    @Test
    public void calangParser_shouldParseCorrect_prog() {
        var program = program("""
                DECLARE  $MESSAGE BYTES
                DECLARE  $LENGTH INTEGER

                BEGIN.
                  CALL password_input $MESSAGE << $TEXT_RECORD
                  CALL bytelength $MESSAGE >> $CHUNK $LENGTH << $LENGTH
                  CALL tower $LENGTH >> $HEIGHT
                """);

        assertTrue(program.getDeclaredInputs().isEmpty());
        assertTrue(program.getDeclaredOutputs().isEmpty());
        assertEquals("BEGIN", program.headParagraphName());

        var scope = program.scope();
        assertSame(BytesValue.class, scope.typeOf("$MESSAGE"));
        assertSame(IntegerValue.class, scope.typeOf("$LENGTH"));

        var main = program.paragraph(program.headParagraphName());
        assertEquals(3, main.instructions().size());
    }

    @Test
    public void calangParser_shouldParseCorrect_byteslength() {
        var program = program("""
                DECLARE INPUT $CHUNK BYTES
                DECLARE OUTPUT $LENGTH INTEGER
                
                START.
                  COMPT IN $LENGTH $CHUNK |.|
                """);

        assertTrue(program.getDeclaredInputs().contains("$CHUNK"));
        assertTrue(program.getDeclaredOutputs().contains("$LENGTH"));
        assertEquals("START", program.headParagraphName());

        var scope = program.scope();
        assertSame(BytesValue.class, scope.typeOf("$CHUNK"));
        assertSame(IntegerValue.class, scope.typeOf("$LENGTH"));

        var main = program.paragraph(program.headParagraphName());
        assertEquals(1, main.instructions().size());
    }

    @Test
    public void calangParser_shouldParseCorrect_performVariations() {
        var program = program("""
                DECLARE $F BOOLEAN
                
                START.
                  PERFORM A IF $F
                  PERFORM A IF NOT $F
                  PERFORM A
                  PERFORM A IF $F ELSE B
                
                A.
                  PRINT dummy
                
                B.
                  PRINT dummy
                """);
        var main = program.paragraph(program.headParagraphName());
        assertEquals(4, main.instructions().size());
    }

    @Test
    public void calangParser_shouldParseCorrect_tower() {
        var program = program("""
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

        assertTrue(program.getDeclaredInputs().contains("$HEIGHT"));
        assertTrue(program.getDeclaredOutputs().isEmpty());
        assertEquals("MAIN", program.headParagraphName());
        assertTrue(List.of("MAIN", "PRINT_LINE", "PRINT_COLUMN").containsAll(program.paragraphNames()));

        var main = program.paragraph(program.headParagraphName());
        assertEquals(3, main.instructions().size());

        var printLine = program.paragraph("PRINT_LINE");
        assertEquals(6, printLine.instructions().size());

        var printColumn = program.paragraph("PRINT_COLUMN");
        assertEquals(3, printColumn.instructions().size());

        var scope = program.scope();
        assertTrue(List.of("$HEIGHT", "$LOCAL_HEIGHT", "$CURSOR", "$FLAG").containsAll(scope.symbolList()));

        assertSame(IntegerValue.class, scope.typeOf("$HEIGHT"));
        assertSame(IntegerValue.class, scope.typeOf("$LOCAL_HEIGHT"));
        assertSame(IntegerValue.class, scope.typeOf("$CURSOR"));
        assertSame(BooleanValue.class, scope.typeOf("$FLAG"));
    }

    static List<String> toLines(String input) {
        return Arrays.asList(input.split("\n"));
    }

    static Calang.Program program(String input) {
        return new Calang().parse(toLines(input));
    }

}
