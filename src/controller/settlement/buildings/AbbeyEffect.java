package src.controller.settlement.buildings;

import src.controller.GameContext;
import src.controller.ICardEffect;

public class AbbeyEffect implements ICardEffect {
    @Override public void apply(GameContext ctx) {
        var p = ctx.current();
        var out = ctx.out();
        p.addProgressPoints(1);
        out.println("Abbey placed: +1 progress point.");
    }
}
