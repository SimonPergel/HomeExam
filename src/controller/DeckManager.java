package src.controller;

import java.util.*;
import src.model.*;
import src.util.Randomizer;
import src.io.interfaces.*;
import src.util.GameConfig;
import java.util.stream.Collectors;

/** Handles stacks, draws, production and exchanges. */
public class DeckManager {
    private final Randomizer rng;
    private final GameConfig cfg;

    // center stacks
    private final Deque<Card> regions = new ArrayDeque<>();
    private final Deque<Card> settlements = new ArrayDeque<>();
    private final Deque<Card> cities = new ArrayDeque<>();
    private final Deque<Card> roads = new ArrayDeque<>();

    // basic draw stacks (4)
    private final List<Deque<BasicCard>> basicStacks =
            List.of(new ArrayDeque<>(), new ArrayDeque<>(), new ArrayDeque<>(), new ArrayDeque<>());

    // event stack
    private final Deque<EventCard> eventStack = new ArrayDeque<>();

    // Assigned dice for the 12 remaining regions (2 per resource type) — requirement 2.c
    private final Map<Resource, Deque<Integer>> regionDicePool = new EnumMap<>(Resource.class);

    /** Placeholder-mode constructor (no JSON available). */
    public DeckManager(Randomizer rng, GameConfig cfg){
        this.rng = rng;
        this.cfg = cfg;
        bootstrapPlaceholders();
    }

    /** Data-mode constructor — feed it CardFactory.BuildResult from cards.json. */
    public DeckManager(Randomizer rng, GameConfig cfg, CardFactory.BuildResult br){
        this.rng = rng;
        this.cfg = cfg;
        loadFromBuildResult(br);
    }
    /** Place a specific basic card at the bottom of the given 1..4 basic stack. */
    public void placeBasicCardToBottom(BasicCard card, int stackIndex1to4){
        int idx = Math.max(1, Math.min(4, stackIndex1to4)) - 1;
        basicStacks.get(idx).addLast(card);
    }

    private void loadFromBuildResult(CardFactory.BuildResult br){
        // center
        for (Card c: br.regions) regions.add(c);
        shuffleDeque(regions); // shuffle region stack at game setup
        for (Card c: br.settlements) settlements.add(c);
        for (Card c: br.cities) cities.add(c);
        for (Card c: br.roads) roads.add(c);

        // Initialize region dice assignment queues (2 per resource type) per requirement 2.c
        initRegionDicePool();

        // events
        for (EventCard e: br.events) eventStack.add(e);

        // basic pool → distribute into 4 stacks round-robin
        int total = br.basicPool.size();
        for (int i = 0; i < total; i++) {
            basicStacks.get(i % 4).add(br.basicPool.get(i));
        }
    }

    private void bootstrapPlaceholders(){
        for (int i=0; i<24; i++) regions.add(new CenterCard("Region"+(i+1), "REGION"));
        shuffleDeque(regions); // shuffle region stack at game setup
        for (int i=0; i<9;  i++) settlements.add(new CenterCard("Settlement"+(i+1), "SETTLEMENT"));
        for (int i=0; i<7;  i++) cities.add(new CenterCard("City"+(i+1), "CITY"));
        for (int i=0; i<9;  i++) roads.add(new CenterCard("Road"+(i+1), "ROAD"));

        for (int i=0; i<36; i++) basicStacks.get(i % 4).add(new BasicCard("Basic"+(i+1)));

        List<EventCard> ev = new ArrayList<>();
        for (int i=0; i<9; i++) ev.add(new EventCard(i==0 ? "YULE" : "Event"+i));
        Collections.shuffle(ev, new java.util.Random(42));
        ev.removeIf(c -> c.getName().equalsIgnoreCase("YULE"));
        ev.add(Math.max(0, ev.size()-3), new EventCard("YULE")); // 4th from bottom
        for (var c: ev) eventStack.add(c);

        initRegionDicePool();
    }

    private void initRegionDicePool(){
        // seed required pairs
        regionDicePool.clear();
        for (Resource r : Resource.values()) regionDicePool.put(r, new ArrayDeque<>());
        // Pairs per rules: Field(3,1) Mountain(4,2) Hill(5,1) Forest(6,2) Pasture(6,5) Gold(3,2)
        pushShuffled(regionDicePool.get(Resource.WHEAT), new int[]{3,1});
        pushShuffled(regionDicePool.get(Resource.ORE),   new int[]{4,2});
        pushShuffled(regionDicePool.get(Resource.BRICK), new int[]{5,1});
        pushShuffled(regionDicePool.get(Resource.WOOD),  new int[]{6,2});
        pushShuffled(regionDicePool.get(Resource.WOOL),  new int[]{6,5});
        pushShuffled(regionDicePool.get(Resource.GOLD),  new int[]{3,2});
    }

    private void pushShuffled(Deque<Integer> dq, int[] vals){
        java.util.List<Integer> list = new java.util.ArrayList<>();
        for (int v: vals) list.add(v);
        // shuffle deterministically via injected Randomizer
        for (int i = list.size()-1; i > 0; i--) {
            int j = rng.nextInt(i+1);
            int tmp = list.get(i);
            list.set(i, list.get(j));
            list.set(j, tmp);
        }
        for (int v: list) dq.addLast(v);
    }

    // Shuffle any deque using our injected Randomizer (Fisher–Yates)
    private <T> void shuffleDeque(Deque<T> dq){
        java.util.ArrayList<T> list = new java.util.ArrayList<>(dq);
        dq.clear();
        for (int i = list.size()-1; i > 0; i--) {
            int j = rng.nextInt(i+1);
            T tmp = list.get(i);
            list.set(i, list.get(j));
            list.set(j, tmp);
        }
        for (T t: list) dq.addLast(t);
    }

    public void dealOpeningHand(Player p, IInputService in, IOutputService out){
        for (int i=0; i<cfg.startHand(); i++){
            int s = promptStackChoice(p, in, out);
            drawFromBasicStack(p, s);
        }
    }

    private int promptStackChoice(Player p, IInputService in, IOutputService out){
        if (p.isBot()) {
            int v = 1 + rng.nextInt(4);
            out.println(p.getName()+" (bot) chooses basic stack " + v + ".");
            return v;
        }
        while (true) {
            out.println(p.getName()+", choose basic stack (1-4):");
            String s = in.readLine();
            try {
                int v = Integer.parseInt((s==null?"":s).trim());
                if (v >= 1 && v <= 4) return v;
            } catch (Exception ignore) {}
            out.println("invalid answer, try again");
        }
    }

    private void drawFromBasicStack(Player p, int stack){
        var card = basicStacks.get(stack-1).pollFirst();
        if (card != null) p.addToHand(card);
    }

    /** Production now fills per-region storage (0..3) on matching dice for both players. */
    public void applyProduction(int prodRoll, Player a, Player b){
        produceFor(a, prodRoll);
        produceFor(b, prodRoll);
    }

    /** +1 stored (clamped to 3) on every region whose die matches prodRoll (all matching tiles produce). */
    private void produceFor(Player p, int roll) {
        var princ = p.principality();
        for (var tile : princ.regions()) if (tile != null && tile.getDie() == roll) tile.incStored();
        // Apply production boosters like Iron Foundry after base production
        applyProductionBoosters(p, roll);
    }

    /** Apply passive building effects that modify production. Currently supports:
     *   - Iron Foundry: Doubles the ore production of the neighboring mountains (same-row, left/right).
     *     If multiple Iron Foundries touch the same mountain, it still only grants +1 extra (no stacking beyond doubling).
     */
    private void applyProductionBoosters(Player p, int roll){
        var princ = p.principality();
        java.util.HashSet<src.model.RegionTile> boosted = new java.util.HashSet<>();

        int cols = princ.width();
        for (int[] site : princ.getOccupiedBuildingSites()){
            int row = site[0];
            int siteCol = site[1];
            String name = princ.getBuildingAt(row, siteCol);
            if (name == null) continue;
            String n = name.trim().toLowerCase();

            // Determine target resource for each production booster building
            Resource target = null;
            if (n.equals("iron foundry")) target = Resource.ORE;
            else if (n.equals("brick factory")) target = Resource.BRICK;
            else if (n.equals("grain mill")) target = Resource.WHEAT;
            else if (n.equals("lumber camp")) target = Resource.WOOD;
            else if (n.equals("weaver's shop") || n.equals("weavers shop") || n.equals("weaver’s shop")) target = Resource.WOOL;

            if (target != null){
                // Buildings on far rows (0/4) should behave like near rows (1/3)
                int effectiveRow = row;
                if (row == 0) effectiveRow = 1;      // far top behaves like top row
                else if (row == 4) effectiveRow = 3; // far bottom behaves like bottom row
                // Only meaningful next to regions on the (effective) same row
                if (effectiveRow != 1 && effectiveRow != 3) continue;
                // Determine the display column where this building appears
                int displayCol = -1;
                for (int c = 0; c < cols; c++){
                    int mapped = princ.mapDisplayColToBuildingSiteCol(c);
                    if (mapped == siteCol) { displayCol = c; break; }
                }
                if (displayCol < 0) continue; // cannot map; skip
                // Check adjacent display columns left/right for regions
                for (int dc : new int[]{-1, +1}){
                    int rc = displayCol + dc;
                    if (rc < 0 || rc >= cols) continue;
                    // Look for neighboring region on the effective row
                    var rt = princ.getRegionAt(effectiveRow, rc);
                    if (rt == null) continue;
                    if (rt.getResource() != target) continue;
                    if (rt.getDie() != roll) continue;
                    if (boosted.contains(rt)) continue; // do not stack more than double
                    rt.incStored(); // add one more to double the production
                    boosted.add(rt);
                }
            }
        }
    }

    // ----------------- Center stack operations -----------------
    public Card drawRoadCard(){ return roads.pollFirst(); }
    public Card drawSettlementCard(){ return settlements.pollFirst(); }
    public Card drawCityCard(){ return cities.pollFirst(); }
    public Card drawRegionCard(){ return regions.pollFirst(); }

    /**
     * Get a snapshot of the region stack (for Scout card to display choices).
     * Returns a read-only list.
     */
    public java.util.List<Card> getRegionStackSnapshot() {
        return new java.util.ArrayList<>(regions);
    }

    /**
     * Draw a specific region card by name or index (for Scout card).
     * Returns the card and removes it from the stack, or null if not found.
     * @param nameOrIndex either the card name (case-insensitive) or a numeric index (0-based)
     */
    public Card drawRegionByChoice(String nameOrIndex) {
        if (nameOrIndex == null || nameOrIndex.trim().isEmpty()) return null;
        
        // Try to parse as index first
        try {
            int idx = Integer.parseInt(nameOrIndex.trim());
            if (idx >= 0 && idx < regions.size()) {
                // Remove card at that index
                java.util.ArrayList<Card> list = new java.util.ArrayList<>(regions);
                Card selected = list.remove(idx);
                regions.clear();
                regions.addAll(list);
                return selected;
            }
        } catch (NumberFormatException ignored) {}
        
        // Try to match by name (case-insensitive)
        String target = nameOrIndex.trim().toLowerCase();
        java.util.ArrayList<Card> list = new java.util.ArrayList<>(regions);
        for (int i = 0; i < list.size(); i++) {
            Card c = list.get(i);
            if (c != null && c.getName() != null && c.getName().toLowerCase().contains(target)) {
                Card selected = list.remove(i);
                regions.clear();
                regions.addAll(list);
                return selected;
            }
        }
        
        return null; // not found
    }

    /** Returns next assigned die for a resource, or empty if no assignment remains. */
    public Optional<Integer> nextAssignedDieFor(Resource r){
        Deque<Integer> dq = regionDicePool.get(r);
        if (dq == null) return Optional.empty();
        Integer v = dq.pollFirst();
        return Optional.ofNullable(v);
    }

    /** Return a generic Settlement center card to the bottom of the settlement stack (used on city upgrade). */
    public void returnSettlementToBottom(){
        settlements.addLast(new CenterCard("Settlement", "SETTLEMENT"));
    }


    /**
     * Replenish at end of turn per rulebook section 3 and legacy flow:
     * - If flagged to skip once (Fraternal Feuds), print message and return.
     * - Target hand size = 3 + progress points.
     * - If below target: repeatedly prompt stack [1-4] and draw from chosen stack until target reached.
     *   If chosen stack is empty, advance circularly to the next non-empty stack; if all empty, stop.
     * - If above target: repeatedly prompt for a card name to discard under a chosen stack until at target.
     */
    public void replenishHand(Player p, IInputService in, IOutputService out){
        if (p.consumeSkipReplenishOnce()) {
            out.println("You cannot replenish your hand this turn (Fraternal Feuds).");
            return;
        }

        int handTarget = 3 + p.getProgressPoints();
        // Draw up to target
        while (p.getHandSize() < handTarget) {
            out.println("PROMPT: Replenish - choose draw stack [1-4]:");
            int which = promptStackChoice(p, in, out);
            Deque<BasicCard> stack = basicStacks.get(which-1);
            if (stack.isEmpty()) {
                // advance circularly until any non-empty
                int tries = 0;
                do {
                    which = 1 + (which % 4);
                    stack = basicStacks.get(which-1);
                    tries++;
                } while (stack.isEmpty() && tries <= 4);
                if (stack.isEmpty()) {
                    out.println("All stacks empty.");
                    break;
                }
            }
            var c = stack.pollFirst();
            if (c != null) p.addToHand(c);
        }

        // Discard down to target if above (rare in intro, but follow rulebook)
        while (p.getHandSize() > handTarget) {
            out.println("PROMPT: You have more than the allowed hand size. Type card index (1.."+p.getHandSize()+") to discard under a stack:");
            try {
                int idx1 = Integer.parseInt(in.readLine().trim()) - 1;
                var snap = p.getHandSnapshot();
                if (idx1 < 0 || idx1 >= snap.size()) idx1 = snap.size()-1;
                BasicCard chosen = p.removeFromHand(idx1);
                out.println("Choose stack [1-4] to put it under:");
                int st = promptStackChoice(p, in, out);
                basicStacks.get(st-1).addLast(chosen);
            } catch (Exception e) {
                // Fallback: drop first to stack 1
                BasicCard chosen = p.removeFromHand(0);
                basicStacks.get(0).addLast(chosen);
            }
        }
    }

    public void optionalExchange(Player p, IInputService in, IOutputService out){
        // Legacy parity (Server.exchangePhase): Only offer exchange if hand >= 3 + progress points
        int limit = 3 + p.getProgressPoints();
        if (p.getHandSize() < limit) {
            out.println("Exchange: hand below limit; skipping.");
            return;
        }

        while (true) {
            out.println("PROMPT: Exchange a card? (Y/N)");
            String ans = in.readLine();
            if (ans == null) return; // treat null as no
            String t = ans.trim().toUpperCase();
            if (t.startsWith("Y")) break; // proceed with exchange
            if (t.startsWith("N")) return; // skip exchange
            out.println("invalid answer, try again");
        }

        BasicCard chosen = null;
        while (chosen == null) {
            out.println("PROMPT: Enter card name to put under a stack (or 'cancel'):");
            String nm = in.readLine();
            if (nm == null) return;
            if (nm.trim().equalsIgnoreCase("cancel")) return;
            chosen = p.removeFromHandByName(nm);
            if (chosen == null) out.println("invalid answer, try again");
        }

    out.println("PROMPT: Choose stack [1-4] to put it under:");
    int st = promptStackChoice(p, in, out); // ensures only 1..4 are accepted
    Deque<BasicCard> stack = basicStacks.get(st - 1);
    stack.addLast(chosen);
    out.println("Placed under stack " + st + " (bottom).");

    // Parish Hall discount: if player has Parish Hall in their principality, search costs 1 instead of 2
    int searchCost = p.principality().hasParishHall() ? 1 : 2;

        String mode;
        while (true) {
            out.println("PROMPT: Choose Random draw (R) or Search (S, costs " + searchCost + " any)?");
            mode = in.readLine();
            if (mode == null) { mode = "R"; break; } // default to Random on EOF
            String m = mode.trim().toUpperCase();
            if (m.startsWith("R") || m.startsWith("S")) { mode = m; break; }
            out.println("invalid answer, try again");
        }
        if (mode.startsWith("S")) {
            // Pay 2 resources of player's choice from storage-only economy
            if (totalStored(p) < searchCost) {
                out.println("Not enough resources to search.");
                return;
            }
            for (int i = 0; i < searchCost; i++) {
                while (true) {
                    out.println("PROMPT: Discard resource #" + (i + 1) + " [Brick|Grain|Lumber|Wool|Ore|Gold]:");
                    String rs = in.readLine();
                    Resource r = parseResource(rs);
                    if (r != null && removeOneStored(p, r)) break;
                    out.println("invalid answer, try again");
                }
            }

            if (stack.isEmpty()) {
                out.println("That stack is empty.");
                return;
            }
            out.println("Stack contains (top..bottom):");
            for (BasicCard c : stack) out.println(" - " + c.getName());
            while (true) {
                out.println("PROMPT: Type exact name to take (or 'cancel'):");
                String take = in.readLine();
                if (take == null) break;
                if (take.trim().equalsIgnoreCase("cancel")) break;
                boolean found = false;
                for (java.util.Iterator<BasicCard> it = stack.iterator(); it.hasNext(); ) {
                    BasicCard c = it.next();
                    if (c.getName() != null && c.getName().equalsIgnoreCase(take)) {
                        it.remove();
                        p.addToHand(c);
                        found = true;
                        break;
                    }
                }
                if (found) break;
                out.println("invalid answer, try again");
            }
        } else {
            // Random draw (top of chosen stack)
            if (stack.isEmpty()) {
                out.println("That stack is empty.");
                return;
            }
            p.addToHand(stack.pollFirst());
        }
    }

    // ---- Exchange helpers ----
    private int safeReadInt(IInputService in, int def){
        try {
            String s = in.readLine();
            if (s == null) return def;
            return Integer.parseInt(s.trim());
        } catch (Exception e){
            return def;
        }
    }

    private int totalStored(Player p){
        int s = 0;
        for (var t : p.principality().regions()) s += Math.max(0, t.getStored());
        return s;
    }

    private boolean removeOneStored(Player p, Resource r){
        for (var t : p.principality().regions()){
            if (t.getResource() != r) continue;
            if (t.getStored() > 0){ t.setStored(t.getStored()-1); return true; }
        }
        return false;
    }

    private boolean consumeAnyStored(Player p){
        for (var r : Resource.values()) if (removeOneStored(p, r)) return true;
        return false;
    }

    private Resource parseResource(String s){
        if (s == null) return null;
        String t = s.trim().toLowerCase();
        switch (t){
            case "brick": return Resource.BRICK;
            case "grain":
            case "wheat": return Resource.WHEAT;
            case "lumber":
            case "wood": return Resource.WOOD;
            case "wool": return Resource.WOOL;
            case "ore": return Resource.ORE;
            case "gold": return Resource.GOLD;
            default: return null;
        }
    }

    public EventCard drawEventCard(){
        var c = eventStack.pollFirst();
        return c != null ? c : new EventCard("Empty");
    }
}