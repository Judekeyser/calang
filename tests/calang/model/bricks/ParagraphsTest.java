package calang.model.bricks;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * This test class is related to testing the parser in a end-to-end manner.
 */
public class ParagraphsTest {

    @Test
    public void paragraphs_shouldParseCorrect_prog() {
        var paragraphs = paragraphs("""
                DECLARE  $MESSAGE BYTES
                DECLARE  $LENGTH INTEGER

                BEGIN.
                  CALL password_input $MESSAGE << $TEXT_RECORD
                  CALL bytelength $MESSAGE >> $CHUNK $LENGTH << $LENGTH
                  CALL tower $LENGTH >> $HEIGHT
                """);

        assertEquals("BEGIN", paragraphs.headParagraph());
        assertEquals(3, paragraphs.paragraphsByName().get(paragraphs.headParagraph()).tokens().size());
    }

    @Test
    public void paragraphs_shouldParseCorrect_byteslength() {
        var paragraphs = paragraphs("""
                DECLARE INPUT $CHUNK BYTES
                DECLARE OUTPUT $LENGTH INTEGER
                
                START.
                
                  COMPT IN $LENGTH $CHUNK |.|
                  
                """);

        assertEquals("START", paragraphs.headParagraph());

        var main = paragraphs.paragraphsByName().get(paragraphs.headParagraph());
        assertEquals(1, main.tokens().size());
    }

    @Test
    public void paragraphs_shouldParseCorrect_performVariations() {
        var paragraphs = paragraphs("""
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
        var main = paragraphs.paragraphsByName().get(paragraphs.headParagraph());
        assertEquals(4, main.tokens().size());

        assertEquals(1, paragraphs.callersOf("A").size());
        assertEquals(1, paragraphs.callersOf("B").size());
    }

    @Test
    public void paragraphs_shouldParseCorrect_tower() {
        var paragraphs = paragraphs("""
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

        assertEquals("MAIN", paragraphs.headParagraph());
        assertTrue(List.of("MAIN", "PRINT_LINE", "PRINT_COLUMN").containsAll(paragraphs.paragraphsByName().keySet()));

        var main = paragraphs.paragraphsByName().get(paragraphs.headParagraph());
        assertEquals(3, main.tokens().size());

        var printLine = paragraphs.paragraphsByName().get("PRINT_LINE");
        assertEquals(6, printLine.tokens().size());

        var printColumn = paragraphs.paragraphsByName().get("PRINT_COLUMN");
        assertEquals(3, printColumn.tokens().size());
    }

    @Test
    public void paragraphs_identifiesNonAsynchronous_stdParagraphs() {
        var paragraphs = paragraphs("""
                MAIN.
                  PRINT Stuff
                """);

        assertFalse(paragraphs.isParagraphAsynchronous("MAIN"));
    }

    @Test
    public void paragraphs_identifiesNonAsynchronous_simpleDepthOne() {
        var paragraphs = paragraphs("""
                MAIN.
                  PERFORM ANOTHER
                
                ANOTHER.
                  PRINT Stuff
                """);

        assertFalse(paragraphs.isParagraphAsynchronous("MAIN"));
        assertFalse(paragraphs.isParagraphAsynchronous("ANOTHER"));
    }

    @Test
    public void paragraphs_identifiesAsynchronous_simpleCall() {
        var paragraphs = paragraphs("""
                MAIN.
                  CALL some_program
                """);

        assertTrue(paragraphs.isParagraphAsynchronous("MAIN"));
    }

    @Test
    public void paragraphs_identifiesAsynchronous_simpleDepthOne() {
        var paragraphs = paragraphs("""
                MAIN.
                  PERFORM ANOTHER
                  
                ANOTHER.
                  CALL some_program
                """);

        assertTrue(paragraphs.isParagraphAsynchronous("ANOTHER"));
        assertTrue(paragraphs.isParagraphAsynchronous("MAIN"));
    }

    @Test
    public void paragraphs_identifiesAsynchronous_lessTrivial() {
        var paragraphs = paragraphs("""
                DECLARE $FLAG BOOLEAN
                
                MAIN.
                  PERFORM ANOTHER
                  PERFORM SYNC_OTHER
                  
                ANOTHER.
                  PERFORM SYNC_OTHER IF $FLAG ELSE ASYNC_OTHER
                
                ASYNC_OTHER.
                  CALL some_program
                
                SYNC_OTHER.
                  PRINT Hello
                """);

        assertFalse(paragraphs.isParagraphAsynchronous("SYNC_OTHER"));
        assertTrue(paragraphs.isParagraphAsynchronous("ASYNC_OTHER"));
        assertTrue(paragraphs.isParagraphAsynchronous("ANOTHER"));
        assertTrue(paragraphs.isParagraphAsynchronous("MAIN"));
    }

    static calang.model.bricks.Paragraphs paragraphs(String input) {
        var lines = input.lines().toList();
        return () -> lines;
    }

}
