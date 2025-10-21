
package src.io;
import java.util.Scanner;
import src.io.interfaces.IInputService;
public class ConsoleInput implements IInputService {
    private final Scanner scanner = new Scanner(System.in);
    public String readLine() { return scanner.nextLine(); }
}
