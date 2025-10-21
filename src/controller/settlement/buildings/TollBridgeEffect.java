package src.controller.settlement.buildings;

import src.controller.GameContext;
import src.controller.ICardEffect;

/**
 * Toll Bridge (1): Settlement/extension building.
 * - When placed: +1 Commerce Point (CP).
 * - Ongoing: During the "Plentiful Harvest" event, each owner receives up to 2 Gold (storage permitting).
 *   The ongoing part is handled by HarvestEffect; this class only accounts for the on-place CP.
 */
public class TollBridgeEffect implements ICardEffect {
    @Override
    public void apply(GameContext ctx) {
        var p = ctx.current();
        // +1 CP on placement
        p.principality().getPoints().addCP(1);
        src.controller.AdvantageManager.updateAll(ctx);
        ctx.out().println("Toll Bridge placed: +1 CP. During Plentiful Harvest you receive up to 2 Gold if space allows.");
    }
}
