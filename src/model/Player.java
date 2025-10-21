package src.model;

import src.io.interfaces.IInputService;
import src.io.interfaces.IOutputService;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumMap;
import java.util.List;

public class Player {
    // Brigitta event die control flag
    private boolean brigittaEventDieControl = false;

    public void markBrigittaEventDieControl() { this.brigittaEventDieControl = true; }
    public boolean consumeBrigittaEventDieControl() {
        if (brigittaEventDieControl) { brigittaEventDieControl = false; return true; }
        return false;
    }
    private final String name;
    private final EnumMap<Resource,Integer> res = new EnumMap<>(Resource.class);
    private final Deque<BasicCard> hand = new ArrayDeque<>();
    private int skillPoints = 0, victoryPoints = 0;
    private boolean tradeAdv = false;
    // Track whether VP for advantages have been awarded to allow removal later
    private boolean tradeAdvVPAwarded = false;
    private boolean strengthAdvVPAwarded = false;
    private boolean bot = false;
    // Skip replenish once (Fraternal Feuds)
    private boolean skipReplenishOnce = false;

    public Player(){ this("Player"); }
    public Player(String name){
        this.name = name;
        for (var r: Resource.values()) res.put(r, 0);
    }

    public String getName(){ return name; }

    public int getSkillPoints(){ return skillPoints; }
    public void setSkillPoints(int v){ skillPoints = v; }

    public boolean hasTradeAdvantage(){ return tradeAdv; }
    public void setTradeAdvantage(boolean b){ tradeAdv = b; }

    public boolean isTradeAdvVPAwarded(){ return tradeAdvVPAwarded; }
    public void setTradeAdvVPAwarded(boolean v){ tradeAdvVPAwarded = v; }
    public boolean isStrengthAdvVPAwarded(){ return strengthAdvVPAwarded; }
    public void setStrengthAdvVPAwarded(boolean v){ strengthAdvVPAwarded = v; }

    public boolean isBot(){ return bot; }
    public void setBot(boolean b){ bot = b; }

    // --- Replenish control (Fraternal Feuds) ---
    public void markSkipReplenishOnce(){ this.skipReplenishOnce = true; }
    /** Returns true if a pending skip was consumed (meaning: you should skip replenish now). */
    public boolean consumeSkipReplenishOnce(){
        if (skipReplenishOnce) { skipReplenishOnce = false; return true; }
        return false;
    }

    public int getVictoryPoints(){ return victoryPoints; }
    public void setVictoryPoints(int v){ victoryPoints = v; }

    public int getResource(Resource r){ return res.get(r); }
    public void setResource(Resource r, int v){ res.put(r, Math.max(0, v)); }
    public void addResource(Resource r, int delta){ res.put(r, Math.max(0, res.get(r) + delta)); }
    public int totalResources(){
        int stored = 0;
        for (var t : principality().regions()) stored += t.getStored();
        return stored;
    }

    // temporary progression counter
    private int progressPoints = 0;
    //public int getProgressPoints(){ return progressPoints; }
    public void setProgressPoints(int v){ progressPoints = Math.max(0, v); }
    //public void addProgressPoints(int delta){ progressPoints = Math.max(0, progressPoints + delta); }

    public void addToHand(BasicCard c){ hand.add(c); }
    public int getHandSize(){ return hand.size(); }

    /** For displaying the hand in Action phase. */
    public List<BasicCard> getHandSnapshot(){
        return new ArrayList<>(hand);
    }

    /** Remove by index (0-based) from the hand. */
    public BasicCard removeFromHand(int index){
        if (index < 0 || index >= hand.size()) return null;
        int i = 0;
        for (var it = hand.iterator(); it.hasNext(); i++) {
            var c = it.next();
            if (i == index) { it.remove(); return c; }
        }
        return null;
    }

    /** Drop the first card to bottom of a basic stack during Exchange. */
    public Card dropOneToBottom(){
        return hand.isEmpty() ? null : hand.pollFirst();
    }

    /** Remove a card from hand by exact name (case-insensitive). */
    public BasicCard removeFromHandByName(String name){
        if (name == null) return null;
        String target = name.trim();
        for (var it = hand.iterator(); it.hasNext(); ){
            BasicCard c = it.next();
            if (c.getName() != null && c.getName().equalsIgnoreCase(target)){
                it.remove();
                return c;
            }
        }
        return null;
    }

    public Resource chooseResource(IInputService in, IOutputService out){
        out.println(name + ", choose resource: Lumber, Brick, Ore, Grain, Wool, Gold");
        try {
            String s = in.readLine();
            if (s == null) return Resource.WOOD;
            s = s.trim().toLowerCase();
            switch (s){
                case "lumber": case "wood": return Resource.WOOD;
                case "brick": return Resource.BRICK;
                case "ore": return Resource.ORE;
                case "grain": case "wheat": return Resource.WHEAT;
                case "wool": return Resource.WOOL;
                case "gold": return Resource.GOLD;
                default: return Resource.WOOD;
            }
        } catch (Exception e){ return Resource.WOOD; }
    }

    public boolean canAfford(java.util.Map<Resource,Integer> cost){
        if (cost == null) return true;
        for (var e : cost.entrySet()){
            if (getResource(e.getKey()) < e.getValue()) return false;
        }
        return true;
    }

    public void pay(java.util.Map<Resource,Integer> cost){
        if (cost == null) return;
        for (var e : cost.entrySet()){
            addResource(e.getKey(), -e.getValue());
        }
    }
    public String resourcesSummary() {
        // Show storage-only counts with friendly resource names.
        StringBuilder sb = new StringBuilder();
        for (var r : Resource.values()) {
            int stored = 0;
            for (var t : principality().regions()) if (t.getResource() == r) stored += t.getStored();
            sb.append(friendly(r)).append(":").append(stored).append(" ");
        }
        return sb.toString().trim();
}

    /** Storage-only across regions, for friendly messages. */
    public String resourcesSummaryTotal() {
        StringBuilder sb = new StringBuilder();
        for (var r : Resource.values()) {
            int stored = 0;
            for (var t : principality().regions()) if (t.getResource() == r) stored += t.getStored();
            sb.append(friendly(r)).append(":").append(stored).append(" ");
        }
        return sb.toString().trim();
    }

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

    


    private final Principality princ = new Principality();
    public Principality principality(){ return princ; }
    
    public int getProgressPoints(){ return princ.getProgressPoints(); }
    public void addProgressPoints(int d){ princ.addProgressPoints(d); }
}
