package src.controller.settlement.buildings;

import src.controller.GameContext;
import src.controller.ICardEffect;

/**
 * Storehouse: Settlement/City building.
 * - Passive: During Brigand Attack, do not count resources on the 2 neighboring regions when checking >7.
 *   The passive is implemented inside BrigandEffect; this class provides placement feedback only.
 */
public class StorehouseEffect implements ICardEffect {
    @Override
    public void apply(GameContext ctx) {
        ctx.out().println("Storehouse placed: protects adjacent regions from Brigand counting.");
    }
}
