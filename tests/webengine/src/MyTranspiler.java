package webengine.src;

import calang.Tangle;
import calang.TranspileJs;
import calang.types.TypedValue;
import calang.types.builtin.BytesValue;
import calang.types.builtin.ProgramValue;

import java.util.List;

import static java.util.Collections.emptyList;

public abstract class MyTranspiler extends TranspileJs implements Tangle, FileContent
{

    public static class ModalElementValue implements TypedValue<ModalElementValue> {}

    {
        addType("MODAL_ELEMENT", ModalElementValue.class);

        addOperator(ModalElementValue.class, "...", ProgramValue.class, emptyList());
        addOperator(ModalElementValue.class, "display!", ModalElementValue.class, emptyList());
        addOperator(ModalElementValue.class, "close!", ModalElementValue.class, emptyList());
        addOperator(ModalElementValue.class, "?", BytesValue.class, emptyList());
    }

    public List<String> transpile(String programName) {
        return transpile(programName, tangle(programName));
    }

    public List<String> tangle(String programName) {
        return tangle(fileContent(programName));
    }

}