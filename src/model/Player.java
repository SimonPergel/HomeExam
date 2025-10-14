package src.model;

import java.util.*;

/**
 * Refactored Player model: contains only state and simple helpers.
 * Note: simple console I/O methods kept here for compatibility with the original server loop.
 * Later we recommend extracting I/O into io interfaces and injecting them.
 */
public class Player {
    public int victoryPoints = 0;
    public int progressPoints = 0;
    public int skillPoints = 0;
    public int commercePoints = 0;
    public int strengthPoints = 0;
    public int tradeRate = 3;
    public boolean isBot = false;

    public Set<String> flags = new HashSet<>();
    public Map<String, Integer> resources = new HashMap<>();
    public List<Card> hand = new ArrayList<>();
    public List<List<Card>> principality = new ArrayList<>();
    public int lastSettlementRow = -1, lastSettlementCol = -1;

    public Player() {
        String[] all = { "Brick", "Grain", "Lumber", "Wool", "Ore", "Gold", "Any" };
        principality = new ArrayList<>();
        for (int r = 0; r < 5; r++) {
            List<Card> row = new ArrayList<>();
            for (int c = 0; c < 5; c++) row.add(null);
            principality.add(row);
        }
        resources = new HashMap<>();
        for (String r : all) resources.put(r, 0);
    }

    // Minimal console-based I/O kept for compatibility with the original run loop.
    private final Scanner in = new Scanner(System.in);
    public void sendMessage(Object m) { System.out.println(m); }
    public String receiveMessage() { System.out.print("> "); return in.nextLine(); }

    // Grid helpers
    public Card getCard(int r, int c) {
        if (r < 0 || c < 0) return null;
        if (r >= principality.size()) return null;
        List<Card> row = principality.get(r);
        if (row == null || c >= row.size()) return null;
        return row.get(c);
    }

    public void placeCard(int r, int c, Card card) {
        ensureSize(r, c);
        principality.get(r).set(c, card);
    }

    public int expandAfterEdgeBuild(int col) {
        int cols = principality.get(0).size();
        if (col == 0) {
            for (List<Card> row : principality) row.add(0, null);
            col += 1;
            if (lastSettlementCol >= 0) lastSettlementCol += 1;
        } else if (col == cols - 1) {
            for (List<Card> row : principality) row.add(null);
        }
        return col;
    }

    private void ensureSize(int r, int c) {
        while (principality.size() <= r) {
            ArrayList<Card> row = new ArrayList<>();
            int cols = principality.isEmpty() ? 5 : principality.get(0).size();
            for (int i = 0; i < cols; i++) row.add(null);
            principality.add(row);
        }
        for (List<Card> row : principality) {
            while (row.size() <= c) row.add(null);
        }
    }

    public boolean hasInPrincipality(String name) {
        for (List<Card> row : principality)
            for (Card c : row)
                if (c != null && c.name != null && c.name.equalsIgnoreCase(name))
                    return true;
        return false;
    }

    public int handSize() { return hand.size(); }
    public void addToHand(Card c) { hand.add(c); }

    public Card removeFromHandByName(String nm) {
        for (int i = 0; i < hand.size(); i++) {
            Card c = hand.get(i);
            if (c != null && c.name != null && c.name.equalsIgnoreCase(nm)) {
                return hand.remove(i);
            }
        }
        return null;
    }

    // Resource/region helpers (kept similar to original)
    private String resourceToRegion(String type) {
        if (type == null) return null;
        String t = type.trim().toLowerCase();
        switch (t) {
            case "brick": return "Hill";
            case "grain": return "Field";
            case "lumber": return "Forest";
            case "wool": return "Pasture";
            case "ore": return "Mountain";
            case "gold": return "Gold Field";
            case "any": return "Any";
            default: return null;
        }
    }

    public List<Card> findRegions(String regionName) {
        List<Card> list = new ArrayList<>();
        if (regionName == null) return list;
        for (int r = 0; r < principality.size(); r++) {
            List<Card> row = principality.get(r);
            if (row == null) continue;
            for (int c = 0; c < row.size(); c++) {
                Card x = row.get(c);
                if (x != null && "Region".equalsIgnoreCase(x.type) && x.name != null && x.name.equalsIgnoreCase(regionName)) {
                    list.add(x);
                }
            }
        }
        return list;
    }

    public int totalAllResources() {
        int sum = 0;
        for (int r = 0; r < principality.size(); r++) {
            List<Card> row = principality.get(r);
            if (row == null) continue;
            for (int c = 0; c < row.size(); c++) {
                Card x = row.get(c);
                if (x != null && "Region".equalsIgnoreCase(x.type)) {
                    sum += Math.max(0, Math.min(3, x.regionProduction));
                }
            }
        }
        return sum;
    }

    public int getResourceCount(String type) {
        String regionName = resourceToRegion(type);
        if (regionName == null) return 0;
        if ("Any".equals(regionName)) return totalAllResources();
        int sum = 0;
        for (Card r : findRegions(regionName)) sum += Math.max(0, Math.min(3, r.regionProduction));
        return sum;
    }

    public void gainResource(String type) {
        String t = type;
        if (t == null || t.equalsIgnoreCase("Any")) {
            sendMessage("PROMPT: Choose resource to gain (Brick/Grain/Lumber/Wool/Ore/Gold):");
            t = receiveMessage();
        }
        String regionName = resourceToRegion(t);
        if (regionName == null || "Any".equals(regionName)) {
            sendMessage("Unknown resource '" + t + "'. Ignored.");
            return;
        }
        List<Card> regs = findRegions(regionName);
        if (regs.isEmpty()) {
            sendMessage("No region for resource " + t + " is present.");
            return;
        }
        Card best = null;
        int bestVal = Integer.MAX_VALUE;
        for (Card r : regs) {
            int v = Math.max(0, Math.min(3, r.regionProduction));
            if (v < bestVal) { bestVal = v; best = r; }
        }
        if (best != null && best.regionProduction < 3) best.regionProduction += 1;
        else sendMessage("No storage space on any " + regionName + " (already 3/3).");
    }

    public boolean removeResource(String type, int n) {
        if (n <= 0) return true;
        String regionName = resourceToRegion(type);
        if (regionName == null || "Any".equals(regionName)) return false;
        List<Card> regs = findRegions(regionName);
        if (regs.isEmpty()) return false;
        int removed = 0;
        while (removed < n) {
            Card best = null;
            int bestVal = -1;
            for (Card r : regs) {
                int v = Math.max(0, Math.min(3, r.regionProduction));
                if (v > bestVal) { bestVal = v; best = r; }
            }
            if (best == null || bestVal <= 0) break;
            best.regionProduction -= 1;
            removed++;
        }
        return removed == n;
    }

    public void setResourceCount(String type, int n) {
        String regionName = resourceToRegion(type);
        if (regionName == null || "Any".equals(regionName)) return;
        List<Card> regs = findRegions(regionName);
        if (regs.isEmpty()) return;
        int maxTotal = regs.size() * 3;
        int want = Math.max(0, Math.min(maxTotal, n));
        int cur = 0;
        for (Card r : regs) { r.regionProduction = Math.max(0, Math.min(3, r.regionProduction)); cur += r.regionProduction; }
        if (cur == want) return;
        if (cur < want) {
            int need = want - cur;
            while (need > 0) {
                Card best = null;
                int bestVal = Integer.MAX_VALUE;
                for (Card r : regs) { int v = r.regionProduction; if (v < 3 && v < bestVal) { bestVal = v; best = r; } }
                if (best == null || best.regionProduction >= 3) break;
                best.regionProduction += 1; need--;
            }
        } else {
            int drop = cur - want;
            while (drop > 0) {
                Card best = null; int bestVal = -1;
                for (Card r : regs) { int v = r.regionProduction; if (v > bestVal) { bestVal = v; best = r; } }
                if (best == null || best.regionProduction <= 0) break;
                best.regionProduction -= 1; drop--;
            }
        }
    }
}
