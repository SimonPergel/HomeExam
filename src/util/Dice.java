
package src.util;
import java.util.function.IntSupplier;
/** Simple die roller. */
public class Dice {
    private final IntSupplier supplier;
    public Dice(IntSupplier supplier) { this.supplier = supplier; }
    public Dice(Randomizer rng) { this(() -> rng.nextInt(6)+1); }
    /** Roll an N-sided die; sides must be >= 2. */
    public int roll(int sides) {
        if (sides <= 1) throw new IllegalArgumentException("sides must be >=2");
        int v = supplier.getAsInt();
        return v % sides + 1; // normalize supplier numbers
    }
}
