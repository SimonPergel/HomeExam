package src.util.policy;
import java.util.Map;
import java.util.List;
/** Policy for mapping die → region production. */
public interface ProductionPolicy {
    Map<String, List<Integer>> regionDice();
}
