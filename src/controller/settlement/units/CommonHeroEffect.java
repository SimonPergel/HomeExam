package src.controller.settlement.units;

import src.controller.ICardEffect;
import src.controller.GameContext;

/**
 * Reusable effect for "Common" heroes: adds SP/FP when the card is played.
 */
public class CommonHeroEffect implements ICardEffect {
    private final int spDelta;
    private final int fpDelta;
    private final String heroName;

    public CommonHeroEffect(int spDelta, int fpDelta, String heroName) {
        this.spDelta = spDelta;
        this.fpDelta = fpDelta;
        this.heroName = heroName;
    }

    @Override
    public void apply(GameContext ctx) {
        var pts = ctx.current().principality().getPoints();
        // Points has addSP/addFP that take deltas
        pts.addSP(spDelta);
        pts.addFP(fpDelta);
    // Re-evaluate advantages due to FP changes
    src.controller.AdvantageManager.updateAll(ctx);
        ctx.out().println(heroName + " joins you: +" + spDelta + " SP, +" + fpDelta + " FP.");
    }
}