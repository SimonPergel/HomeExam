
package src.model;
import java.util.*;
public class Deck<T extends Card> {
    private final Deque<T> stack = new ArrayDeque<>();
    public void pushTop(T c){ stack.addFirst(c); }
    public T draw(){ return stack.pollFirst(); }
    public void pushBottom(T c){ stack.addLast(c); }
    public int size(){ return stack.size(); }
    public boolean isEmpty(){ return stack.isEmpty(); }
}
