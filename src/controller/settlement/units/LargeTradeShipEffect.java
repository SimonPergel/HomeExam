package src.controller.settlement.units;

import src.controller.GameContext;
import src.controller.ICardEffect;

/**
 * Large Trade Ship: when placed, grants +1 Commerce Point (CP).
 * Adjacency trading is handled by the LTS command in GameController.
 */
public class LargeTradeShipEffect implements ICardEffect {
    @Override
    public void apply(GameContext ctx) {
        var pts = ctx.current().principality().getPoints();
        pts.addCP(1);
        src.controller.AdvantageManager.updateAll(ctx);
        ctx.out().println("Large Trade Ship placed: +1 CP. Use 'LTS <L|R> <2from> <1to>' to trade with the adjacent region.");
    }
}
 
