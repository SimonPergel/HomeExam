
package src.io.interfaces;
public interface IOutputService {
    void println(String s);
    default void print(String s) { println(s); }
}
