package src.controller.actions;

import src.controller.GameContext;
import src.model.Resource;
import src.controller.ICardEffect;

public class MerchantEffect implements ICardEffect {
    @Override public void apply(GameContext ctx){
        var r = ctx.current().chooseResource(ctx.in(), ctx.out());
        ctx.current().addResource(r, 2);
        ctx.out().println("Merchant: " + ctx.current().getName() + " gains +2 " + r);
    }
}
