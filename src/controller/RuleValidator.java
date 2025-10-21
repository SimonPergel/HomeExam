package src.controller;

import src.model.BasicCard;
import src.model.Player;
import src.model.Resource;
import src.util.CostParser;
import src.util.GameConfig;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

/**
 * Central place for rule checks & explanations (Introductory/Base game).
 * - SRP: validation + payments + friendly explanations (no I/O, no deck ops).
 * - OCP: add prereq checks per card when you model board/placement later.
 */
public class RuleValidator {

    private final GameConfig cfg;

    public RuleValidator(GameConfig cfg) {
        this.cfg = cfg;
    }

    /** Base-game victory condition. */
    public boolean hasWon(Player p) {
        return p.getVictoryPoints() >= cfg.victoryPointsToWin();
    }

    // ---------------------------- COSTS ----------------------------

    /** True if player can pay the cost of the given basic card using region storage only (no bank). */
    public boolean canPlayBasicCard(Player p, BasicCard card) {
        Map<Resource,Integer> cost = CostParser.parse(card.getCost());
        return canAffordUsingStorage(p, cost);
    }

    /** Deduct the cost (consume from region storage only). Caller should check affordability first. */
    public void payCost(Player p, BasicCard card) {
        Map<Resource,Integer> cost = CostParser.parse(card.getCost());
        payUsingStorage(p, cost);
    }
    // --- NEW: strength advantage helper ---
    public boolean hasStrengthAdvantage(src.model.Player a, src.model.Player b) {
        int fa = a.principality().getPoints().getFP();  // strength points (FP) of A
        int fb = b.principality().getPoints().getFP();  // strength points (FP) of B
        return fa >= 3 && fa > fb;                      // threshold 3, strictly greater
    }

    /** Returns map of missing resources (only those where player lacks enough). Empty = can afford. */
    public Map<Resource,Integer> missingCost(Player p, BasicCard card) {
        Map<Resource,Integer> cost = CostParser.parse(card.getCost());
        EnumMap<Resource,Integer> missing = new EnumMap<>(Resource.class);
        for (var e : cost.entrySet()) {
            int need = e.getValue();
            int available = totalAvailable(p, e.getKey());
            if (available < need) {
                missing.put(e.getKey(), need - available);
            }
        }
        return missing;
    }

    /** Human-readable message explaining what’s missing for cost, or null if affordable. */
    public String explainCostFailure(Player p, BasicCard card) {
        Map<Resource,Integer> miss = missingCost(p, card);
        if (miss.isEmpty()) return null;
        StringBuilder sb = new StringBuilder();
        sb.append("You cannot afford: ").append(card.getName());
    String cost = card.getCost();
    if (cost != null && !cost.isBlank()) sb.append(" (cost: ")
        .append(src.util.CostParser.legacyToNewLetters(cost)).append(")");
        sb.append(". Missing: ");
        boolean first = true;
        for (var e : miss.entrySet()) {
            if (!first) sb.append(", ");
            first = false;
            sb.append(e.getValue()).append(" ").append(friendly(e.getKey()));
        }
        sb.append(". Your storage -> ").append(p.resourcesSummaryTotal());
        return sb.toString();
    }

    // -------------------- Storage-aware helpers --------------------
    private boolean canAffordUsingStorage(Player p, Map<Resource,Integer> cost) {
        if (cost == null) return true;
        for (var e : cost.entrySet()) {
            if (totalAvailable(p, e.getKey()) < e.getValue()) return false;
        }
        return true;
    }

    private int totalAvailable(Player p, Resource r) {
        int stored = 0;
        for (var t : p.principality().regions()) if (t.getResource() == r) stored += t.getStored();
        return stored;
    }

    private void payUsingStorage(Player p, Map<Resource,Integer> cost) {
        if (cost == null) return;
        for (var e : cost.entrySet()) {
            Resource r = e.getKey();
            int need = e.getValue();
            // Consume from region storage first
            var regions = p.principality().regions();
            // simple order: drain any tiles with stored > 0
            for (var tile : regions) {
                if (need == 0) break;
                if (tile.getResource() != r) continue;
                int s = tile.getStored();
                if (s <= 0) continue;
                int take = Math.min(s, need);
                tile.setStored(s - take);
                need -= take;
            }
            // No bank fallback: if need remains, do nothing (caller should have checked affordability)
        }
    }

    // -------------------- DYNAMIC EFFECT PRECONDITIONS --------------------

    /**
     * Returns null if preconditions are met; otherwise a message explaining why the card
     * cannot be played right now (e.g., timing, dynamic resources independent of cost).
     *
     * Keep base-game only here. Add/extend when you model board/placement.
     */
    public String effectPreconditionFailure(Player p, BasicCard card) {
        String n = (card.getName() == null ? "" : card.getName().toLowerCase(Locale.ROOT).trim());

        switch (n) {
            case "goldsmith":
                // Storage-only economy: needs 3 GOLD stored across Gold regions
                int g = totalAvailable(p, Resource.GOLD);
                if (g < 3) return "Goldsmith requires 3 GOLD to play; you have " + g + ".";
                return null;

            case "merchant caravan":
                // Rule: must discard exactly 2 resources (any mix) -> needs total resources >= 2
                if (p.totalResources() < 2)
                    return "Merchant Caravan requires discarding exactly 2 resources; you currently have "
                            + p.totalResources() + ".";
                return null;

            // Add other base cards with dynamic prerequisites here as needed.

            default:
                return null; // no extra preconditions
        }
    }
    // Friendly resource-name helper for messages
    private static String friendly(Resource r){
        switch (r){
            case WOOD:  return "Lumber";
            case BRICK: return "Brick";
            case ORE:   return "Ore";
            case WHEAT: return "Grain";
            case WOOL:  return "Wool";
            case GOLD:  return "Gold";
            default:    return r.name();
        }
    }
}