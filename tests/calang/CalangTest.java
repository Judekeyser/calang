package calang;

import calang.model.types.TypedValue;
import calang.model.types.BooleanValue;
import calang.model.types.BytesValue;
import calang.model.types.IntegerValue;
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
            assertNotNull("Basic Calang should support %s type".formatted(s), calang.typesMap.typeForSymbol(s));
    }

    @Test
    public void basicCalang_shouldSupport_basicOperators() {
        var calang = new Calang();

        for (String s : new String[]{"NEQ", "PREC", "SUCC"})
            assertNotNull("Basic Calang should support %s operator on INTEGER".formatted(s),
                    calang.operatorsMap.operatorForName(IntegerValue.class, s));

        for (String s : new String[]{"NEGATE", "AND", "OR", "XAND", "XOR", "IMPLIES"})
            assertNotNull("Basic Calang should support %s operator on BOOLEAN".formatted(s),
                    calang.operatorsMap.operatorForName(BooleanValue.class, s));

        for (String s : new String[]{"|.|"})
            assertNotNull("Basic Calang should support %s operator on BYTES".formatted(s),
                    calang.operatorsMap.operatorForName(BytesValue.class, s));
    }

    @Test
    public void basicCalang_shouldSupport_addType() {
        var calang = new Calang();
        var magnet = "MY_TYPE";

        class MyType implements TypedValue<MyType> {}

        calang.addType(magnet, MyType.class);

        assertSame(calang.typesMap.typeForSymbol(magnet), MyType.class); // null yields false
    }

    @Test
    public void basicCalang_shouldSupport_addOperator() {
        var calang = new Calang();

        calang.addOperator(IntegerValue.class, "|.|", IntegerValue.class, emptyList());

        assertNotNull(calang.operatorsMap.operatorForName(IntegerValue.class, "|.|"));
    }

}
