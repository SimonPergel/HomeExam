package src.controller.event;

import src.controller.GameContext;
import src.controller.ICardEffect;

/**
 * Trade Ships Race (1):
 * If no player has built a trade ship, no one receives the resource.
 *
 * NOTE: This modifies production for the current event/turn. Since production
 * is already applied in TurnManager before/after events depending on die,
 * we log the rule. If you track "hasTradeShip" in Principality, you can detect
 * and zero-out the resource from the production roll here.
 */
public class TradeShipsRaceEffect implements ICardEffect {
    @Override public void apply(GameContext ctx) {
        // TODO: integrate with your board model, example:
        // boolean aHas = ctx.current().principality().hasTradeShip();
        // boolean bHas = ctx.opponent().principality().hasTradeShip();
        // if (!aHas && !bHas) ctx.decks().revertLastProduction(ctx.current(), ctx.opponent());
        ctx.out().println("Event: Trade Ships Race — If no player has built a trade ship, no one receives the resource. (TODO: enforce when ship presence is modeled.)");
    }
}