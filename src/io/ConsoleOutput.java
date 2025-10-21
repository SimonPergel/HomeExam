
package src.io;
import src.io.interfaces.IOutputService;
public class ConsoleOutput implements IOutputService {
    public void println(String s) { System.out.println(s); }
}
