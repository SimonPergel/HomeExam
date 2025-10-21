
package src.controller.eventDieEvents;

import src.controller.GameContext;
import src.model.Player;
import src.model.Resource;
import src.model.RegionTile;
import src.controller.ICardEffect;

public class BrigandEffect implements ICardEffect {
    @Override public void apply(GameContext ctx){
        int a = applyTo(ctx.current());
        int b = applyTo(ctx.opponent());
        ctx.out().println("Event: Brigand — players with >7 in storage lose ALL Wool and Gold.");
        ctx.outOpponent().println("Event: Brigand — players with >7 in storage lose ALL Wool and Gold.");
        if (a > 0) {
            String msg = ctx.current().getName()+": Brigand removed "+a+" tokens (Wool/Gold).";
            ctx.out().println(msg);
            ctx.outOpponent().println(msg);
        }
        if (b > 0) {
            String msg = ctx.opponent().getName()+": Brigand removed "+b+" tokens (Wool/Gold).";
            ctx.out().println(msg);
            ctx.outOpponent().println(msg);
        }
    }

    /** Returns total tokens removed (Wool + Gold) from storage for player p. */
    private int applyTo(Player p){
        // Storehouse passive: do not count the two neighboring regions per Storehouse when checking the >7 threshold.
        int total = 0;
        var princ = p.principality();
        java.util.Set<Integer> protectedCols = princ.getRegionColsProtectedByStorehouses();
        int cols = princ.width();
        for (int c = 0; c < cols; c++){
            if (protectedCols.contains(c)) continue; // skip counting for protected columns
            RegionTile top = princ.getRegionAt(1, c);
            RegionTile bot = princ.getRegionAt(3, c);
            if (top != null) total += Math.max(0, top.getStored());
            if (bot != null) total += Math.max(0, bot.getStored());
        }
        if (total <= 7) return 0;
        int removed = 0;
        for (RegionTile t : p.principality().regions()){
            if (t.getResource() == Resource.WOOL || t.getResource() == Resource.GOLD){
                removed += t.getStored();
                if (t.getStored() > 0) t.setStored(0);
            }
        }
        return removed;
    }
}
