
package src.controller.eventDieEvents;

import src.controller.GameContext;
import src.model.Resource;
import src.controller.ICardEffect;
import src.model.Player;
import src.model.RegionTile;

public class CelebrationEffect implements ICardEffect {
    @Override public void apply(GameContext ctx){
        var c = ctx.current(); var o = ctx.opponent();
        int spC = c.principality().getPoints().getSP();
        int spO = o.principality().getPoints().getSP();

        if (spC > spO){
            ctx.out().println("Celebration: you have more skill points.");
            var r = c.chooseResource(ctx.in(), ctx.out());
            boolean ok = addOneToStorage(c, r);
            broadcast(ctx, c.getName() + (ok ? " gains 1 " + r : " chose " + r + " but had no storage space."));
        } else if (spO > spC){
            // Opponent chooses on their own terminal
            ctx.outOpponent().println("Celebration: you have more skill points.");
            var r = o.chooseResource(ctx.inOpponent(), ctx.outOpponent());
            boolean ok = addOneToStorage(o, r);
            broadcast(ctx, o.getName() + (ok ? " gains 1 " + r : " chose " + r + " but had no storage space."));
        } else {
            // Both choose independently on their own terminals
            ctx.out().println("Celebration: equal skill points — you choose a resource.");
            var rc = c.chooseResource(ctx.in(), ctx.out());
            ctx.outOpponent().println("Celebration: equal skill points — you choose a resource.");
            var ro = o.chooseResource(ctx.inOpponent(), ctx.outOpponent());

            boolean okC = addOneToStorage(c, rc);
            boolean okO = addOneToStorage(o, ro);
            broadcast(ctx, "Celebration: both players chose. "
                + c.getName() + (okC ? " +1 " + rc : " no space for " + rc) + ", "
                + o.getName() + (okO ? " +1 " + ro : " no space for " + ro) + ".");
        }
    }

    private void broadcast(GameContext ctx, String msg){
        ctx.out().println(msg);
        ctx.outOpponent().println(msg);
    }

    /** Storage-only economy: place +1 token on a region of that resource with available capacity (0..3). */
    private boolean addOneToStorage(Player p, Resource r){
        RegionTile best = null;
        int bestStored = Integer.MAX_VALUE;
        for (var t : p.principality().regions()){
            if (t.getResource() == r && t.getStored() < 3){
                if (t.getStored() < bestStored){ bestStored = t.getStored(); best = t; }
            }
        }
        if (best == null) return false;
        best.incStored();
        return true;
    }
}
