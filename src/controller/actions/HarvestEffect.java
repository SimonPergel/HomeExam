package src.controller.actions;

import src.controller.ICardEffect;
import src.controller.GameContext;
import src.model.Player;
import src.model.Resource;
import src.model.RegionTile;

/** Event-die face 4: Plentiful Harvest. Each player chooses a resource on their own terminal and gains +1 if storage allows. */
public class HarvestEffect implements ICardEffect {
    @Override public void apply(GameContext ctx){
        var c = ctx.current();
        var o = ctx.opponent();

    // Prompt active player with validation (invalid answers and no-space reprompt)
    Resource rc = promptResourceWithCapacity(ctx.in(), ctx.out(), c);
    addOneToStorage(c, rc); // guaranteed to have space

    // Then prompt the waiting player on their terminal with the same validation
    Resource ro = promptResourceWithCapacity(ctx.inOpponent(), ctx.outOpponent(), o);
    addOneToStorage(o, ro);

    // Toll Bridge ongoing effect: owner receives up to 2 Gold (storage permitting)
    int goldC = applyTollBridgeGold(c);
    int goldO = applyTollBridgeGold(o);

    String msg = "Plentiful Harvest: "
        + c.getName() + " +1 " + rc
        + (goldC > 0 ? ", +" + goldC + " Gold (Toll Bridge)" : "") + ", "
        + o.getName() + " +1 " + ro
        + (goldO > 0 ? ", +" + goldO + " Gold (Toll Bridge)" : "")
        + ".";
        ctx.out().println(msg);
        ctx.outOpponent().println(msg);
    }

    /** Storage-only economy: place +1 on a region of that resource with capacity (0..3), preferring the lowest stored. */
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

    /** If the player has a Toll Bridge placed, add up to 2 Gold to storage (respecting cap). Returns the amount added. */
    private int applyTollBridgeGold(Player p){
        if (!p.principality().hasBuildingNamed("Toll Bridge")) return 0;
        int added = 0;
        for (int i = 0; i < 2; i++){
            if (addOneToStorage(p, Resource.GOLD)) added++;
            else break; // no more capacity
        }
        return added;
    }

    // ---- Interactive helpers for Plentiful Harvest ----
    private Resource promptResourceWithCapacity(src.io.interfaces.IInputService in,
                                                src.io.interfaces.IOutputService out,
                                                Player p) {
        while (true) {
            out.println(p.getName() + ", choose resource: Lumber, Brick, Ore, Grain, Wool, Gold");
            String line = in.readLine();
            if (line == null) line = "lumber"; // fallback
            Resource r = parse(line);
            if (r == null) {
                out.println("invalid answer, please try again");
                continue;
            }
            if (!hasCapacityFor(p, r, 1)) {
                out.println("no space for that resource, try again");
                continue;
            }
            return r;
        }
    }

    private Resource parse(String s){
        if (s == null) return null;
        String t = s.trim().toLowerCase(java.util.Locale.ROOT);
        switch (t){
            case "lumber": case "wood": case "l": return Resource.WOOD;
            case "brick": case "b": return Resource.BRICK;
            case "ore": case "o": return Resource.ORE;
            case "grain": case "wheat": case "g": case "h": return Resource.WHEAT;
            case "wool": case "sheep": case "w": return Resource.WOOL;
            case "gold": case "a": return Resource.GOLD;
            default: return null;
        }
    }

    private boolean hasCapacityFor(Player p, Resource r, int add){
        int need = add;
        for (var t : p.principality().regions()){
            if (t.getResource() != r) continue;
            int cap = Math.max(0, 3 - t.getStored());
            if (cap <= 0) continue;
            need -= cap;
            if (need <= 0) return true;
        }
        return need <= 0;
    }
}
