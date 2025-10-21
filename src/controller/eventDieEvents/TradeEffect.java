
package src.controller.eventDieEvents;


import src.controller.GameContext;
import src.controller.AdvantageManager;
import src.model.Resource;
import src.controller.ICardEffect;
import src.model.RegionTile;

public class TradeEffect implements ICardEffect {
    @Override public void apply(GameContext ctx){
        // Re-evaluate advantages in case CP changed earlier this turn
        AdvantageManager.updateAll(ctx);
        if (!ctx.current().hasTradeAdvantage()) { return; }
        ctx.out().println("Trade advantage: choose a resource to take from opponent's storage (if available):");
        Resource choice = ctx.current().chooseResource(ctx.in(), ctx.out());
        boolean removed = removeOneFromStorage(ctx.opponent(), choice);
        if (removed) {
            boolean added = addOneToStorage(ctx.current(), choice);
            String msg = ctx.current().getName() + (added ? " gains 1 " + choice : " had no space for " + choice + ". Resource lost.");
            ctx.out().println(msg);
            ctx.outOpponent().println(msg);
        } else {
            String msg = "Opponent has no " + choice + " in storage to take.";
            ctx.out().println(msg);
            ctx.outOpponent().println(msg);
        }
    }

    private boolean removeOneFromStorage(src.model.Player p, Resource r){
        for (RegionTile t : p.principality().regions()){
            if (t.getResource() == r && t.getStored() > 0){
                t.setStored(t.getStored() - 1);
                return true;
            }
        }
        return false;
    }

    private boolean addOneToStorage(src.model.Player p, Resource r){
        RegionTile best = null;
        int bestStored = Integer.MAX_VALUE;
        for (RegionTile t : p.principality().regions()){
            if (t.getResource() == r && t.getStored() < 3){
                if (t.getStored() < bestStored){ bestStored = t.getStored(); best = t; }
            }
        }
        if (best == null) return false;
        best.setStored(best.getStored() + 1);
        return true;
    }
}
