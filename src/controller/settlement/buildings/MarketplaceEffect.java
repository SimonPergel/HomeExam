package src.controller.settlement.buildings;

import src.controller.GameContext;
import src.controller.ICardEffect;

/**
 * Marketplace: Settlement/City building.
 * - When placed: +1 Commerce Point (CP).
 * - Ongoing: If a production die is rolled that appears more often on the opponent's regions than on yours,
 *   you receive 1 resource of your choice that your opponent can normally receive. The ongoing effect is
 *   handled in GameController.produce(); this class only accounts for the on-place CP and user feedback.
 */
public class MarketplaceEffect implements ICardEffect {
    @Override
    public void apply(GameContext ctx) {
        var p = ctx.current();
        p.principality().getPoints().addCP(1);
        src.controller.AdvantageManager.updateAll(ctx);
        ctx.out().println("Marketplace placed: +1 CP. When opponent has more of a rolled number, you may take 1 resource they can normally receive.");
    }
}
