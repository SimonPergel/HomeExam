package src.controller.actions;

import src.model.*;
import src.controller.GameContext;
import src.controller.ICardEffect;

public class GoldsmithEffect implements ICardEffect {
    @Override
    public void apply(GameContext ctx) {
        var p = ctx.current();
        var in = ctx.in();
        var out = ctx.out();

        // Use STORAGE-only gold (on gold regions) as per refactor storage economy
        int goldStored = totalStored(p, Resource.GOLD);
        if (goldStored < 3) {
            out.println("Goldsmith requires 3 GOLD to play; you have " + goldStored + ".");
            return;
        }
        // pay 3 gold from storage tiles
        consumeStored(p, Resource.GOLD, 3);

        // gain any 2 resources (choices may be same or different) with capacity checks
    out.println("Goldsmith: choose your FIRST resource to gain.");
    Resource g1 = promptGain(in, out, p);
        addStored(p, g1, 1);
    out.println("Goldsmith: choose your SECOND resource to gain.");
    Resource g2 = promptGain(in, out, p);
        addStored(p, g2, 1);
    out.println("Goldsmith resolved: +1 " + g1 + ", +1 " + g2 + ".");
    }

    // ----- storage helpers (mirrors GameController/DeckManager patterns) -----
    private int totalStored(Player p, Resource r){
        int s = 0;
        for (var t : p.principality().regions()) if (t.getResource() == r) s += t.getStored();
        return s;
    }
    private void consumeStored(Player p, Resource r, int amount){
        int need = amount;
        for (var t : p.principality().regions()){
            if (need == 0) break;
            if (t.getResource() != r) continue;
            int s = t.getStored();
            if (s <= 0) continue;
            int take = Math.min(s, need);
            t.setStored(s - take);
            need -= take;
        }
    }
    private void addStored(Player p, Resource r, int amount){
        int need = amount;
        for (var t : p.principality().regions()){
            if (need == 0) break;
            if (t.getResource() != r) continue;
            int cap = Math.max(0, 3 - t.getStored());
            if (cap <= 0) continue;
            int add = Math.min(cap, need);
            t.setStored(t.getStored() + add);
            need -= add;
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
    private Resource promptGain(src.io.interfaces.IInputService in, src.io.interfaces.IOutputService out, Player p){
        while (true){
            out.println("Choose a resource to gain: Lumber, Brick, Ore, Grain, Wool, Gold");
            String line = in.readLine();
            if (line == null) line = "lumber";
            Resource r = parse(line);
            if (r == null){ out.println("try again, invalid answer"); continue; }
            if (!hasCapacityFor(p, r, 1)){ out.println("no space for that resource, try again"); continue; }
            return r;
        }
    }
}
