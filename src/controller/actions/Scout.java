package src.controller.actions;

import src.controller.ICardEffect;
import src.controller.GameContext;

/**
 * Scout action card: May only be used when building a new settlement.
 * Allows the player to choose 2 specific regions from the region stack
 * instead of drawing the top 2 cards automatically.
 */
public class Scout implements ICardEffect {
    
    @Override
    public void apply(GameContext ctx) {
        // Set a flag in TurnState indicating Scout was played
        // The SettlementPlacementHandler will check this flag and allow region selection
        if (ctx.turn() != null) {
            ctx.turn().setScoutActive(true);
        }
        ctx.out().println("Scout played: When you build your next settlement, you may choose 2 regions from the region stack.");
    }
}
