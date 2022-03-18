package calang.transpilation;

@FunctionalInterface
public interface Transpiler<T> {
    T transpile();
}
