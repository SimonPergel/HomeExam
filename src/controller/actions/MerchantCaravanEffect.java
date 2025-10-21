package src.controller.actions;

import src.model.*;
import src.controller.GameContext;
import src.controller.ICardEffect;

import java.util.Locale;

public class MerchantCaravanEffect implements ICardEffect {

    @Override
    public void apply(GameContext ctx) {
        var p = ctx.current();
        var in = ctx.in();
        var out = ctx.out();

        if (p.totalResources() < 2) {
            out.println("Merchant Caravan: you need at least 2 total resources to discard.");
            return;
        }

        // Show what the player has
        out.println("You have -> " + p.resourcesSummary());

        // Ask for two discards, accept "lumber brick" or "wool wool"
        Resource d1 = null, d2 = null;
        while (true) {
            out.println("Discard exactly TWO resources (e.g., 'lumber brick' or 'wool wool').");
            String line = in.readLine();
            if (line == null) return;
            line = line.trim();
            String[] parts = line.split("\\s+");
            if (parts.length == 2) {
                d1 = parseResource(parts[0]);
                d2 = parseResource(parts[1]);
            }
            if (d1 != null && d2 != null) {
                // validate availability using STORAGE ONLY (region tiles)
                int haveD1 = totalStored(p, d1);
                int haveD2 = totalStored(p, d2);
                if (d1 == d2) { if (haveD1 >= 2) break; }
                else { if (haveD1 >= 1 && haveD2 >= 1) break; }
                out.println("Not enough to discard those. You have -> " + p.resourcesSummary());
            } else {
                out.println("Invalid answer, try again. Try names like: lumber, brick, ore, grain, wool, gold.");
            }
        }

        // apply discards from STORAGE (drain across tiles)
        consumeStored(p, d1, 1);
        consumeStored(p, d2, 1);

        // Choose two resources to gain; ensure capacity exists in STORAGE
        Resource[] gains;
        while (true) {
            gains = chooseTwoResources(in, out, p, "Now choose ANY TWO resources to gain (e.g., 'lumber lumber' or 'lumber brick').");
            Resource g1 = gains[0];
            Resource g2 = gains[1];
            if (g1 == null || g2 == null) continue;
            int needG1 = 1;
            int needG2 = (g1 == g2) ? 2 : 1;
            int capG1 = capacityFor(p, g1);
            int capG2 = capacityFor(p, g2);
            boolean ok;
            if (g1 == g2) ok = capG1 >= 2; else ok = (capG1 >= 1 && capG2 >= 1);
            if (!ok) {
                out.println("Not enough storage space for that choice. Current storage -> " + p.resourcesSummary());
                continue;
            }
            break;
        }

        addStored(p, gains[0], 1);
        addStored(p, gains[1], 1);
        out.println("Merchant Caravan done. You now have -> " + p.resourcesSummary());

    }

    // Accepts full names (lumber/brick/ore/grain/wool/gold) and common shortcuts.
    private Resource parseResource(String s) {
        if (s == null) return null;
        String t = s.trim().toLowerCase(Locale.ROOT);
        switch (t) {
            // Lumber (legacy: wood)
            case "lumber":
            case "wood":
            case "timber":
            case "log":
            case "l":              // letter mapping to Lumber
                return Resource.WOOD;
            case "brick":
            case "b":               // letter mapping to Brick
                return Resource.BRICK;
            case "ore":
            case "o":               // letter mapping to Ore
                return Resource.ORE;
            // Grain (legacy: wheat)
            case "grain":
            case "wheat":
            case "g":               // letter mapping to Grain
            case "h":               // legacy H for wheat/grain
                return Resource.WHEAT;
            case "wool":
            case "sheep":
            case "w":               // letter mapping to Wool
                return Resource.WOOL;
            case "gold":
            case "gld":
            case "a":               // letter mapping A for gold per card letters
                return Resource.GOLD;
            default:
                return null;
        }
}

    // ===== Storage-only helpers (mirror GameController trades) =====
    private int totalStored(Player p, Resource r){
        int s = 0;
        for (var t : p.principality().regions()) if (t.getResource() == r) s += t.getStored();
        return s;
    }
    private int capacityFor(Player p, Resource r){
        int cap = 0;
        for (var t : p.principality().regions()) if (t.getResource() == r) cap += Math.max(0, 3 - t.getStored());
        return cap;
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

    // A one-resource prompt using full names; avoids letter ambiguity
    private Resource chooseResourceByName(src.io.interfaces.IInputService in,
                                          src.io.interfaces.IOutputService out,
                                          Player p) {
        while (true) {
            out.println(p.getName() + ", choose resource by name: lumber, brick, ore, grain, wool, gold");
            String line = in.readLine();
            if (line == null) return Resource.WOOD;
            Resource r = parseResource(line);
            if (r != null) return r;
            out.println("Invalid answer, try again. Try: lumber, brick, ore, grain, wool, gold.");
        }
    }
    private Resource[] chooseTwoResources(src.io.interfaces.IInputService in,
                                      src.io.interfaces.IOutputService out,
                                      Player p,
                                      String prompt) {
        while (true) {
            out.println(prompt);
            String line = in.readLine();
            if (line == null) return new Resource[]{Resource.WOOD, Resource.WOOD};
            line = line.trim();
            String[] parts = line.split("\\s+");
            if (parts.length == 2) {
                Resource r1 = parseResource(parts[0]);
                Resource r2 = parseResource(parts[1]);
                if (r1 != null && r2 != null) return new Resource[]{r1, r2};
                out.println("Invalid answer, try again. Try: lumber, brick, ore, grain, wool, gold.");
                continue;
            }
            // If only one provided, accept it and prompt once more for the second
            if (parts.length == 1) {
                Resource r1 = parseResource(parts[0]);
                if (r1 == null) {
                    out.println("Invalid answer, try again. Try: lumber, brick, ore, grain, wool, gold.");
                    continue;
                }
                out.println("Choose the SECOND resource to gain:");
                Resource r2 = chooseResourceByName(in, out, p);
                return new Resource[]{r1, r2};
            }
            out.println("Invalid answer, try again. Examples: 'lumber lumber' or 'lumber brick'.");
        }
}
}
