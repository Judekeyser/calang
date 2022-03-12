package calang;

import calang.scopes.Scope;

import java.util.List;

public interface Program<T> {
    List<String> paragraphNames();

    Paragraph<T> paragraph(String name);

    String headParagraphName();

    Scope scope();

    default List<String> getDeclaredOutputs() {
        return scope().outputsList();
    }

    default List<String> getDeclaredInputs() {
        return scope().inputsList();
    }
}
