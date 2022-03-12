package calang;

import calang.types.TypedValue;
import calang.types.BooleanValue;
import calang.types.BytesValue;
import calang.types.IntegerValue;
import org.junit.Test;

import static java.util.Collections.emptyList;
import static org.junit.Assert.*;

/**
 * Parsing test is done in the CalangParseE2ETest file.
 */
public class CalangTest {

    @Test
    public void basicCalang_shouldSupport_basicTypes() {
        var calang = new Calang();

        for (String s : new String[]{"INTEGER", "PROGRAM", "BYTES", "BOOLEAN"})
            assertTrue("Basic Calang should support %s type".formatted(s), calang.TOKENS.containsKey(s));
    }

    @Test
    public void basicCalang_shouldSupport_basicOperators() {
        var calang = new Calang();

        for (String s : new String[]{"NEQ", "PREC", "SUCC"})
            assertNotNull("Basic Calang should support %s operator on INTEGER".formatted(s),
                    calang.operatorsMap.maybeOperator(IntegerValue.class, s));

        for (String s : new String[]{"NEGATE", "AND", "OR", "XAND", "XOR", "IMPLIES"})
            assertNotNull("Basic Calang should support %s operator on BOOLEAN".formatted(s),
                    calang.operatorsMap.maybeOperator(BooleanValue.class, s));

        for (String s : new String[]{"|.|"})
            assertNotNull("Basic Calang should support %s operator on BYTES".formatted(s),
                    calang.operatorsMap.maybeOperator(BytesValue.class, s));
    }

    @Test
    public void basicCalang_shouldSupport_addType() {
        var calang = new Calang();
        var magnet = "MY_TYPE";

        assertFalse(calang.TOKENS.containsKey(magnet));

        class MyType implements TypedValue<MyType> {}

        calang.addType(magnet, MyType.class);

        assertTrue(calang.TOKENS.containsKey(magnet));
        assertSame(calang.TOKENS.get(magnet), MyType.class); // null yields false
    }

    @Test
    public void basicCalang_shouldSupport_addOperator() {
        var calang = new Calang();

        calang.addOperator(IntegerValue.class, "|.|", IntegerValue.class, emptyList());

        assertNotNull(calang.operatorsMap.maybeOperator(IntegerValue.class, "|.|"));
    }

}
