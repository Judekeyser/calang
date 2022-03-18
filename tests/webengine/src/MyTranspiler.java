package webengine.src;

import calang.Calang;
import calang.Tangle;
import calang.model.types.TypedValue;
import calang.model.types.BytesValue;
import calang.model.types.ProgramValue;

import java.util.List;

import static java.util.Collections.emptyList;

public abstract class MyTranspiler extends Calang implements Tangle, FileContent
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