
package src.util;
import java.util.Random;
/** Injectable RNG to make tests deterministic. */
public class Randomizer {
    private final Random rnd;
    public Randomizer() { this(new Random()); }
    public Randomizer(long seed) { this(new Random(seed)); }
    public Randomizer(Random rnd) { this.rnd = rnd; }
    public int nextInt(int bound) { return rnd.nextInt(bound); } // 0..bound-1
}
