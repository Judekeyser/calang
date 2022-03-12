package calang.model.algebra;

import calang.Calang;
import calang.model.Program;
import org.junit.Test;

import java.util.List;

import static calang.model.algebra.GraphLinks.asynchronousParagraphs;

import static org.junit.Assert.*;

public class GraphLinksTest {

    @Test
    public void async_identifiesNonAsynchronous_stdParagraphs() {
        var program = programOf("""
                MAIN.
                  PRINT Stuff
                """);

        var asynchronous = asynchronousParagraphs(program);
        assertTrue(asynchronous.isEmpty());
    }

    @Test
    public void async_identifiesNonAsynchronous_simpleDepthOne() {
        var program = programOf("""
                MAIN.
                  PERFORM ANOTHER
                
                ANOTHER.
                  PRINT Stuff
                """);

        var asynchronous = asynchronousParagraphs(program);
        assertTrue(asynchronous.isEmpty());
    }

    @Test
    public void async_identifiesAsynchronous_simpleCall() {
        var program = programOf("""
                MAIN.
                  CALL some_program
                """);

        var asynchronous = asynchronousParagraphs(program);
        assertFalse(asynchronous.isEmpty());
        assertEquals(1, asynchronous.size());
    }

    @Test
    public void async_identifiesAsynchronous_simpleDepthOne() {
        var program = programOf("""
                MAIN.
                  PERFORM ANOTHER
                  
                ANOTHER.
                  CALL some_program
                """);

        var asynchronous = asynchronousParagraphs(program);
        assertFalse(asynchronous.isEmpty());
        assertEquals(2, asynchronous.size());
    }

    @Test
    public void async_identifiesAsynchronous_lessTrivial() {
        var program = programOf("""
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

        var asynchronous = asynchronousParagraphs(program);
        assertFalse(asynchronous.isEmpty());
        assertEquals(3, asynchronous.size());
    }

    static Program<Calang.PreInstruction> programOf(String sheet) {
        class CalangExtension extends Calang {
            @Override
            protected Program<PreInstruction> parse(List<String> lines) {
                return super.parse(lines);
            }
        }
        return new CalangExtension().parse(sheet.lines().toList());
    }

}
