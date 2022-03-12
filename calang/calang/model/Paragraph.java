package calang.model;

import java.util.List;

@FunctionalInterface
public interface Paragraph<T> {

    List<T> instructions();

}
