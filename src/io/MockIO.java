
package src.io;
import java.util.ArrayDeque;
import src.io.interfaces.*;
public class MockIO implements IInputService, IOutputService, IPlayerIO {
    private final ArrayDeque<String> scripted = new ArrayDeque<>();
    private final StringBuilder log = new StringBuilder();
    public MockIO script(String... lines){ for(String l:lines) scripted.add(l); return this; }
    public String readLine(){ return scripted.isEmpty()? "1": scripted.removeFirst(); }
    public void println(String s){ log.append(s).append('\n'); }
    public IInputService in(){ return this; }
    public IOutputService out(){ return this; }
    public String getLog(){ return log.toString(); }
}
