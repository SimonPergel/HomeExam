package src.controller.settlement.units;

import src.controller.GameContext;
import src.controller.ICardEffect;
import src.model.Resource;

/** Reusable effect for all Common trade ships: 2:1 for one resource + (JSON shows) +1 CP. */
public class CommonTradeShipEffect implements ICardEffect {
    private final Resource resource;
    private final String displayName;

    public CommonTradeShipEffect(Resource resource, String displayName) {
        this.resource = resource;
        this.displayName = displayName;
    }

    @Override
    public void apply(GameContext ctx) {
        // Grant 2:1 privilege for the specific resource
        ctx.current().principality().grantTradeShip(resource);

        // Common trade ships give +1 CP in your JSON
    var pts = ctx.current().principality().getPoints();
    // Add +1 CP (delta)
    pts.addCP(1);

        // Re-evaluate advantages due to CP change
        src.controller.AdvantageManager.updateAll(ctx);
        ctx.out().println(displayName + " built: you may trade " + resource + " at 2:1 (unlimited this turn and future turns). +1 CP.");
    }
}