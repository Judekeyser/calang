package calang.model;

import java.util.List;

public interface Program<T> {

    List<String> paragraphNames();

    Paragraph<T> paragraph(String name);

    String headParagraphName();

    Scope scope();
}
