
package src.util;
import java.util.*;
/** Centralized tunables; override for eras/expansions. */
public class GameConfig {
    public int centerCards() { return 49; }
    public int basicCards()  { return 36; }
    public int eventCards()  { return 9; }
    public int startHand()   { return 3; }
    public int yuleFromBottom() { return 4; }
    public int victoryPointsToWin() { return 7; }
    public int replenishDrawCount() { return 1; }
    // Center card costs (Intro game) used in action menu: ROAD(BBL), SETTLEMENT(BGLW), CITY(GGOOO)
    // Legend for costs: L=Lumber, B=Brick, W=Wool, O=Ore, G=Grain, A=Gold
    public String roadCost() { return "BBL"; }
    public String settlementCost() { return "BGLW"; } // Brick, Grain, Lumber, Wool
    public String cityCost() { return "GGOOO"; }      // 2x Grain + 3x Ore
    /** Map region type name -> list of production die pips that produce there. */
    public Map<String, List<Integer>> regionDice() {
        return Map.of(
            "FIELD", List.of(3,1),
            "MOUNTAIN", List.of(4,2),
            "HILL", List.of(5,1),
            "FOREST", List.of(6,2),
            "PASTURE", List.of(6,5),
            "GOLD_FIELD", List.of(3,2)
        );
    }
}
