package src.controller;

import src.model.Player;
import src.model.BasicCard;
import src.io.interfaces.IInputService;
import src.io.interfaces.IOutputService;
import src.io.interfaces.IPlayerIO;
import src.util.GameConfig;
import src.util.Randomizer;


import java.util.Arrays;

public class GameController {
    private final TurnManager turns;
    private TurnState currentTurnState;
    private final DeckManager decks;
    private final RuleValidator rules;
    private final Randomizer rng;
    private final GameConfig cfg;
    private final IInputService in;
    private final IOutputService out;
    // Removed direct EffectCatalog field; effects are resolved by card/effect instances elsewhere.

    // Placement registry (SOLID-friendly)
    private final src.controller.placement.PlacementRegistry placementRegistry;

    // Per-player IO mapping (for split terminals)
    private final java.util.IdentityHashMap<Player, IPlayerIO> playerIO = new java.util.IdentityHashMap<>();

    public GameController(TurnManager t, DeckManager d, RuleValidator r,
                          Randomizer rng, GameConfig cfg,
                          IInputService in, IOutputService out){
        this.turns = t;
        this.decks = d;
        this.rules = r;
        this.rng = rng;
        this.cfg = cfg;
        this.in = in;
        this.out = out;

        this.placementRegistry = new src.controller.placement.PlacementRegistry(
            Arrays.asList(
                new src.controller.placement.RoadPlacementHandler(),
                new src.controller.placement.SettlementPlacementHandler(),
                new src.controller.placement.CityPlacementHandler(),
                new src.controller.placement.ParishHallPlacementHandler(),
                new src.controller.placement.AbbeyPlacementHandler(),
                new src.controller.placement.GenericBuildingPlacementHandler(),
                new src.controller.placement.RegionPlacementHandler(),
                new src.controller.placement.HeroPlacementHandler()
            )
        );
    }

    /** Register IO for a specific player (console or socket-backed). */
    public void registerPlayerIO(Player p, IPlayerIO io) {
        if (p != null && io != null) playerIO.put(p, io);
    }

    private IPlayerIO ioFor(Player p) {
        IPlayerIO io = playerIO.get(p);
        if (io != null) return io;
        return new IPlayerIO() {
            @Override public IInputService in() { return in; }
            @Override public IOutputService out() { return out; }
        };
    }

    /** Seed starting region storage like the old server UI: non-gold 1/3, gold 0/3. */
    public void seedStartingStorageFor(src.model.Player p){
        var princ = p.principality();
        for (int row : new int[]{0, 2}) {
            for (int col = 0; col < 5; col++) {
                var t = princ.getRegionAt(row, col);
                if (t == null) continue;
                if (t.getResource() == src.model.Resource.GOLD) t.setStored(0);
                else t.setStored(1);
            }
        }
    }

    /** Convenience to seed and then show both boards at start. */
    public void showStartingBoards(src.model.Player me, src.model.Player opp){
        seedStartingStorageFor(me);
        seedStartingStorageFor(opp);

        out.println("Opponent's starting board:");
        src.view.BoardPrinter.printPlayerBoard(opp, out);

        out.println("Your starting board:");
        src.view.BoardPrinter.printPlayerBoard(me, out);
    }

    public void setCurrentTurnState(TurnState ts) { this.currentTurnState = ts; }

    public GameContext makeContext(Player current, Player opponent){
        IPlayerIO io = ioFor(current);
        IPlayerIO ioOpp = ioFor(opponent);
        return new GameContext(current, opponent, io.in(), io.out(), ioOpp.in(), ioOpp.out(), decks, rules, currentTurnState);
    }

    public void produce(int roll, Player a, Player b){
        decks.applyProduction(roll, a, b);
    // Marketplace passive: if opponent has more regions showing this number, gain 1 resource your opponent can normally receive
        applyMarketplaceBonus(roll, a, b);
        applyMarketplaceBonus(roll, b, a);
        // Broadcast production die in legacy format
        var ioA = ioFor(a); var ioB = ioFor(b);
        ioA.out().println("[ProductionDie] -> " + roll);
        ioB.out().println("[ProductionDie] -> " + roll);
    }

    // --- Victory helper: check both players and announce winner; return true if game should end ---
    private boolean checkVictory(Player a, Player b) {
        if (rules.hasWon(a)) { declareWinner(a); return true; }
        if (rules.hasWon(b)) { declareWinner(b); return true; }
        return false;
    }

    // --- Marketplace ongoing effect ---
    private void applyMarketplaceBonus(int roll, Player p, Player opp){
        var princP = p.principality();
        if (!princP.hasBuildingNamed("Marketplace")) return;
        int countP = countRegionsWithDie(princP, roll);
        int countO = countRegionsWithDie(opp.principality(), roll);
        if (countO <= countP) return; // only when opponent has strictly more

        // Allowed resources = any resource the opponent can normally receive (has at least one region of that type)
        java.util.EnumSet<src.model.Resource> allowed = java.util.EnumSet.noneOf(src.model.Resource.class);
        for (var t : opp.principality().regions()) if (t != null) allowed.add(t.getResource());
        // Prompt current player to choose one; enforce storage capacity
        var io = ioFor(p);
        src.model.Resource chosen = null;
        for (int tries = 0; tries < 10; tries++){
            io.out().println("Marketplace: Opponent has more regions with number " + roll + ". Choose 1 resource they can normally receive ("+friendlyList(allowed)+"): ");
            String line = io.in().readLine();
            src.model.Resource r = parseResource(line);
            if (r == null || !allowed.contains(r)) { io.out().println("Invalid choice, try again."); continue; }
            if (!hasCapacityFor(p, r, 1)) { io.out().println("No space to store that resource; choose another."); continue; }
            chosen = r; break;
        }
        if (chosen == null) return; // no valid choice made
        addStored(p, chosen, 1);
        var ioOpp = ioFor(opp);
        String msg = p.getName()+" receives +1 "+friendly(chosen)+" from Marketplace.";
        io.out().println(msg);
        ioOpp.out().println(msg);
    }

    private int countRegionsWithDie(src.model.Principality pr, int die){
        int n = 0;
        var m = pr.produce(die); // returns counts per resource
        for (var e : m.values()) n += e;
        return n;
    }

    // storage helpers are defined later in the class (trade section)

    public void broadcastEventDie(int face, Player a, Player b){
        var ioA = ioFor(a); var ioB = ioFor(b);
        ioA.out().println("[EventDie] -> " + face);
        ioB.out().println("[EventDie] -> " + face);
    }

    public void broadcastLine(String s, Player a, Player b){
        var ioA = ioFor(a); var ioB = ioFor(b);
        ioA.out().println(s);
        ioB.out().println(s);
    }

    public void printTurnRecap(Player a, Player b){
        // For player A's terminal
        var ioA = ioFor(a);
        ioA.out().println("Opponent's board:");
        src.view.BoardPrinter.printPlayerBoard(b, ioA.out());
        ioA.out().println("Your board:");
        src.view.BoardPrinter.printPlayerBoard(a, ioA.out());
        ioA.out().println("Your hand:");
        java.util.List<BasicCard> handA = a.getHandSnapshot();
        ioA.out().println("Hand ("+handA.size()+"):" );
        for (int i=0;i<handA.size();i++){
            var c = handA.get(i);
            String cost = c.getCost();
            if (cost != null && !cost.isBlank()) cost = src.util.CostParser.legacyToNewLetters(cost);
            String pts = summarizePoints(c);
            ioA.out().println("  ["+i+"] " + c.getName() + badge(cost, "cost") + badge(pts, null));
            if (c.getDescription()!=null && !c.getDescription().isBlank()) ioA.out().println("    "+c.getDescription());
        }

        // For player B's terminal
        var ioB = ioFor(b);
        ioB.out().println("Opponent's board:");
        src.view.BoardPrinter.printPlayerBoard(a, ioB.out());
        ioB.out().println("Your board:");
        src.view.BoardPrinter.printPlayerBoard(b, ioB.out());
        ioB.out().println("Your hand:");
        java.util.List<BasicCard> handB = b.getHandSnapshot();
        ioB.out().println("Hand ("+handB.size()+"):" );
        for (int i=0;i<handB.size();i++){
            var c = handB.get(i);
            String cost = c.getCost();
            if (cost != null && !cost.isBlank()) cost = src.util.CostParser.legacyToNewLetters(cost);
            String pts = summarizePoints(c);
            ioB.out().println("  ["+i+"] " + c.getName() + badge(cost, "cost") + badge(pts, null));
            if (c.getDescription()!=null && !c.getDescription().isBlank()) ioB.out().println("    "+c.getDescription());
        }
    }

    /** Number-based action phase: play multiple cards until 'skip'. */
    public void actionPhase(Player current, Player opponent){
        // Before starting the interactive loop, show both updated boards and hand again
        // to the active player (mirroring the legacy flow), and mirror to the other
        // terminal WITHOUT the action menu so they just wait.
        {
            var ioA = ioFor(current);
            ioA.out().println("Opponent's board:");
            src.view.BoardPrinter.printPlayerBoard(opponent, ioA.out());
            ioA.out().println("Your board:");
            src.view.BoardPrinter.printPlayerBoard(current, ioA.out());
            ioA.out().println("Your hand:");
            var handA = current.getHandSnapshot();
            ioA.out().println("Hand ("+handA.size()+"):" );
            for (int i=0;i<handA.size();i++){
                var c = handA.get(i);
                String cost = c.getCost();
                if (cost != null && !cost.isBlank()) cost = src.util.CostParser.legacyToNewLetters(cost);
                String pts = summarizePoints(c);
                ioA.out().println("  ["+i+"] " + c.getName() + badge(cost, "cost") + badge(pts, null));
                if (c.getDescription()!=null && !c.getDescription().isBlank()) ioA.out().println("    "+c.getDescription());
            }

            var ioB = ioFor(opponent);
            ioB.out().println("Opponent's board:");
            src.view.BoardPrinter.printPlayerBoard(current, ioB.out());
            ioB.out().println("Your board:");
            src.view.BoardPrinter.printPlayerBoard(opponent, ioB.out());
            ioB.out().println("Your hand:");
            var handB = opponent.getHandSnapshot();
            ioB.out().println("Hand ("+handB.size()+"):" );
            for (int i=0;i<handB.size();i++){
                var c = handB.get(i);
                String cost = c.getCost();
                if (cost != null && !cost.isBlank()) cost = src.util.CostParser.legacyToNewLetters(cost);
                String pts = summarizePoints(c);
                ioB.out().println("  ["+i+"] " + c.getName() + badge(cost, "cost") + badge(pts, null));
                if (c.getDescription()!=null && !c.getDescription().isBlank()) ioB.out().println("    "+c.getDescription());
            }
        }

        while (true) {
            var io = ioFor(current);
            var in = io.in();
            var out = io.out();
            if (current.getHandSize() == 0) {
                out.println("Action phase: hand is empty. You may type 'road', 'settlement', 'city', 'region' or 'skip'.");
                String cmd = in.readLine();
                if (cmd==null) return;
                cmd = cmd.trim().toLowerCase();
                if (handleCenterCommand(cmd, current, opponent)) continue;
                return; // skip or unknown
            }

            // Legacy-like action phase menu
            out.println("Action Phase:");
            out.println("  TRADE3 <get> <give>  —  bank 3:1 ([Brick|Grain|Lumber|Wool|Ore|Gold])");
            out.println("  TRADE2 <get> <Res>   —  if you have a 2:1 ship for <Res> ([Brick|Grain|Lumber|Wool|Ore|Gold])");
            out.println("  LTS <L|R> <2from> <1to>  —  Large Trade Ship adjacent trade (left/right side) ([Brick|Grain|Lumber|Wool|Ore|Gold])");
            out.println("  PLAY <cardName> | <id>  —  play a card from hand / play center card: ROAD("+cfg.roadCost()+"), SETTLEMENT("+cfg.settlementCost()+"), CITY("+cfg.cityCost()+")");
                out.println("  END          —  finish action phase");
            out.println("PROMPT: make your choice:");

            String line = in.readLine();
            if (line == null) return;
            line = line.trim();
            // Legacy-like parser: support PLAY <center>
            String lower = line.toLowerCase();
            // --- Trades and LTS (legacy-like commands) ---
            if (lower.startsWith("trade3")) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length >= 3) {
                    String getStr = parts[1];
                    String giveStr = parts[2];
                    var getR = parseResource(getStr);
                    var giveR = parseResource(giveStr);
                    if (getR == null || giveR == null) {
                        out.println("Usage: TRADE3 <get> <give> ([Brick|Grain|Lumber|Wool|Ore|Gold])");
                        continue;
                    }
                    if (totalStored(current, giveR) < 3) {
                        out.println("Not enough " + giveR + " to trade 3:1.");
                        continue;
                    }
                    if (!hasCapacityFor(current, getR, 1)) {
                        out.println("No storage space for " + getR + " (all regions at cap).");
                        continue;
                    }
                    // perform trade
                    consumeStored(current, giveR, 3);
                    addStored(current, getR, 1);
                    out.println("Trade 3:1 -> +1 " + getR);
                    ioFor(opponent).out().println("Trade 3:1 -> +1 " + getR);
                    // Show updated board and hand to active player after action
                    printBoardAndHand(current, out);
                    if (checkVictory(current, opponent)) return; // end immediately on win
                } else {
                    out.println("Usage: TRADE3 <get> <give> ([Brick|Grain|Lumber|Wool|Ore|Gold])");
                }
                continue;
            }
            if (lower.startsWith("trade2")) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length >= 3) {
                    String getStr = parts[1];
                    String fromStr = parts[2];
                    var getR = parseResource(getStr);
                    var fromR = parseResource(fromStr);
                    if (getR == null || fromR == null) {
                        out.println("Usage: TRADE2 <get> <give> ([Brick|Grain|Lumber|Wool|Ore|Gold])");
                        continue;
                    }
                    // require a 2:1 ship for the <from> resource
                    if (current.principality().getTradeRatio(fromR) != 2) {
                        out.println("You don't have a 2:1 ship for " + fromR + ".");
                        continue;
                    }
                    if (totalStored(current, fromR) < 2) {
                        out.println("Not enough " + fromR + " to trade 2:1.");
                        continue;
                    }
                    if (!hasCapacityFor(current, getR, 1)) {
                        out.println("No storage space for " + getR + " (all regions at cap).");
                        continue;
                    }
                    consumeStored(current, fromR, 2);
                    addStored(current, getR, 1);
                    out.println("Trade 2:1 (" + fromR + " ship) -> +1 " + getR);
                    ioFor(opponent).out().println("Trade 2:1 (" + fromR + " ship) -> +1 " + getR);
                    // Show updated board and hand to active player after action
                    printBoardAndHand(current, out);
                    if (checkVictory(current, opponent)) return;
                } else {
                    out.println("Usage: TRADE2 <get> <give> ([Brick|Grain|Lumber|Wool|Ore|Gold])");
                }
                continue;
            }
            if (lower.startsWith("lts")) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length < 4) {
                    out.println("Usage: LTS <L|R> <2from> <1to> ([Brick|Grain|Lumber|Wool|Ore|Gold])");
                    continue;
                }
                String side = parts[1].trim().toUpperCase();
                if (!("L".equals(side) || "R".equals(side))) {
                    out.println("Side must be L or R.");
                    continue;
                }
                src.model.Resource fromR = parseResource(parts[2]);
                src.model.Resource toR = parseResource(parts[3]);
                if (fromR == null || toR == null) {
                    out.println("Usage: LTS <L|R> <2from> <1to> ([Brick|Grain|Lumber|Wool|Ore|Gold])");
                    continue;
                }

                var pr = current.principality();
                if (!pr.hasBuildingNamed("Large Trade Ship")) {
                    out.println("You don't have a Large Trade Ship placed.");
                    continue;
                }
                // Collect all LTS locations
                java.util.List<int[]> sites = pr.getOccupiedBuildingSites();
                int[] chosenSite = null; // row,col of LTS used
                var width = pr.width();
                boolean foundMatchingResource = false; // has an LTS with correct adj resource but <2 stored
                for (int[] rcSite : sites) {
                    int br = rcSite[0], bc = rcSite[1];
                    String nm = pr.getBuildingAt(br, bc);
                    if (nm == null || !nm.equalsIgnoreCase("Large Trade Ship")) continue;
                    int adjCol = bc + ("R".equals(side) ? 1 : -1);
                    if (adjCol < 0 || adjCol >= width) continue;
                    // Parity: if LTS is on far rows (0 or 4), treat adjacency as if on 1 or 3 respectively
                    int effectiveRow = (br == 0) ? 1 : (br == 4 ? 3 : br);
                    var adjReg = pr.getRegionAt(effectiveRow, adjCol);
                    if (adjReg == null) continue;
                    if (adjReg.getResource() != fromR) continue; // not the requested resource
                    foundMatchingResource = true;
                    if (adjReg.getStored() >= 2) { chosenSite = rcSite; break; }
                }
                if (chosenSite == null) {
                    if (foundMatchingResource)
                        out.println("Adjacent region does not have at least 2 " + fromR + " to trade.");
                    else
                        out.println("No adjacent region with resource " + fromR + " on that side of your Large Trade Ship.");
                    continue;
                }
                if (!hasCapacityFor(current, toR, 1)) {
                    out.println("No storage space for " + toR + " (all regions at cap).");
                    continue;
                }
                // Perform trade against that single adjacent region
                int br = chosenSite[0], bc = chosenSite[1];
                int adjCol = bc + ("R".equals(side) ? 1 : -1);
                int effectiveRow = (br == 0) ? 1 : (br == 4 ? 3 : br);
                var adjReg = pr.getRegionAt(effectiveRow, adjCol);
                adjReg.setStored(adjReg.getStored() - 2);
                addStored(current, toR, 1);
                out.println("LTS trade: -2 " + fromR + " (adjacent " + side + "), +1 " + toR + ".");
                ioFor(opponent).out().println("LTS trade: -2 " + fromR + " (adjacent " + side + "), +1 " + toR + ".");
                printBoardAndHand(current, out);
                if (checkVictory(current, opponent)) return;
                continue;
            }
            if (lower.startsWith("play ")) {
                String what = line.substring(5).trim();
                // If user typed a center card name, route to center; otherwise try to play a hand card by name or id
                String wl = what.toLowerCase();
                if (wl.equals("road") || wl.equals("settlement") || wl.equals("city") || wl.equals("region")) {
                    handleCenterCommand(wl, current, opponent);
                } else {
                    boolean ok = playHandCardByToken(what, current, opponent);
                    if (!ok) {
                        out.println("Couldn't find a playable card matching '" + what + "' in your hand.");
                    }
                }
                continue;
            } else if (lower.equals("road") || lower.equals("settlement") || lower.equals("city") || lower.equals("region")) {
                handleCenterCommand(lower, current, opponent); // handled attempt (success or not)
                continue;
            } else if (lower.equals("end") || lower.equals("skip")) {
                // End action phase; TurnManager will proceed to Replenish and Exchange.
                return;
            }

            int index;
            try {
                index = Integer.parseInt(line.trim()) - 1; // hand is displayed 1-based
            } catch (Exception e) {
                out.println("Not a number.");
                continue;
            }
            java.util.List<BasicCard> hand = current.getHandSnapshot();
            if (index < 0 || index >= hand.size()) {
                out.println("Out of range.");
                continue;
            }

            // Peek (don’t remove yet)
            var chosen = hand.get(index);

            // 1) Cost check with friendly explanation
            String costMsg = rules.explainCostFailure(current, chosen);
            if (costMsg != null) {
                out.println(costMsg);
                out.println("Pick another card or type 'skip'");
                continue;
            }

            // 2) Dynamic precondition check with friendly explanation
            String preMsg = rules.effectPreconditionFailure(current, chosen);
            if (preMsg != null) {
                out.println(preMsg);
                out.println("Pick another card or type 'skip'");
                continue;
            }

            // 3) All good → remove, pay
            chosen = current.removeFromHand(index);
            rules.payCost(current, chosen);

            // 4) Try placement via registry (roads/settlements/cities/regions)
            boolean placed = this.placementRegistry.tryPlace(chosen, makeContext(current, opponent));
            boolean wasPlaceable = this.placementRegistry.canHandle(chosen);

            // Optional: refund & return if user cancelled/failed placement
            if (wasPlaceable && !placed) {
                out.println("Placement failed/cancelled — refunding cost and returning card to hand.");
                // If you add a refund helper later:
                // rules.refundCost(current, chosen);
                current.addToHand(chosen);
                continue;
            }

            // 5) If placed, show board and check victory; otherwise run effect (action/hero/etc.)
            if (placed) {
                printBoardAndHand(current, out);
                if (checkVictory(current, opponent)) return;
            } else {
                var eff = chosen.getEffect();
                if (eff != null) {
                    eff.apply(makeContext(current, opponent));
                    out.println("Played: " + chosen.getName());
                    // After any action effect resolves, show updated board + hand
                    printBoardAndHand(current, out);
                    if (checkVictory(current, opponent)) return;
                } else {
                    out.println("Played: " + chosen.getName() + " (no effect wired yet).");
                    printBoardAndHand(current, out);
                    if (checkVictory(current, opponent)) return;
                }
            }
            // loop again so the player can play more cards or 'skip'
        }
    }

    /** Try to play a card from hand given a token which can be either a 1-based index or a case-insensitive name. */
    private boolean playHandCardByToken(String token, Player current, Player opponent){
        var io = ioFor(current);
        var out = io.out();
        var hand = current.getHandSnapshot();

        Integer idx = null;
        try { idx = Integer.parseInt(token.trim()); } catch (Exception ignore) {}
        if (idx != null) idx = idx - 1; // hand menu uses 1-based for quick typing

        int targetIndex = -1;
        if (idx != null && idx >= 0 && idx < hand.size()) {
            targetIndex = idx;
        } else {
            // find by name (case-insensitive, exact match first, then contains)
            String t = token.trim().toLowerCase();
            for (int i=0;i<hand.size();i++) {
                String n = hand.get(i).getName();
                if (n != null && n.trim().equalsIgnoreCase(token)) { targetIndex = i; break; }
            }
            if (targetIndex < 0) {
                for (int i=0;i<hand.size();i++) {
                    String n = hand.get(i).getName();
                    if (n != null && n.toLowerCase().contains(t)) { targetIndex = i; break; }
                }
            }
            if (targetIndex < 0) return false;
        }

        var chosen = hand.get(targetIndex);

        // 1) Cost check
        String costMsg = rules.explainCostFailure(current, chosen);
        if (costMsg != null) { out.println(costMsg); return true; }
        // 2) Preconditions
        String preMsg = rules.effectPreconditionFailure(current, chosen);
        if (preMsg != null) { out.println(preMsg); return true; }
        // 3) Remove and pay
        chosen = current.removeFromHand(targetIndex);
        rules.payCost(current, chosen);
        // 4) Try placement first
        boolean placed = this.placementRegistry.tryPlace(chosen, makeContext(current, opponent));
        boolean wasPlaceable = this.placementRegistry.canHandle(chosen);
        if (wasPlaceable && !placed) {
            out.println("Placement failed/cancelled — refunding cost and returning card to hand.");
            current.addToHand(chosen);
            return true;
        }
        // 5) Otherwise apply effect
        if (!placed) {
            var eff = chosen.getEffect();
            if (eff != null) {
                eff.apply(makeContext(current, opponent));
                out.println("Played: " + chosen.getName());
            } else {
                out.println("Played: " + chosen.getName() + " (no effect wired yet).");
            }
            // show updated board + hand
            printBoardAndHand(current, out);
        }
        return true;
    }

    private boolean handleCenterCommand(String cmd, Player current, Player opponent){
        switch (cmd) {
            case "road":
                return placeCenterCard(new src.model.CenterCard("Road", "ROAD"), current, opponent);
            case "settlement":
                return placeCenterCard(new src.model.CenterCard("Settlement", "SETTLEMENT"), current, opponent);
            case "city":
                return placeCenterCard(new src.model.CenterCard("City", "CITY"), current, opponent);
            case "region":
                return placeRegionFromCenter(current, opponent);
            default:
                return false;
        }
    }

    private boolean placeCenterCard(src.model.CenterCard cc, Player current, Player opponent){
        var io = ioFor(current);
        // Enforce center card costs via config
        String cost = null;
        switch (cc.getCategory()){
            case "ROAD": cost = cfg.roadCost(); break;
            case "SETTLEMENT": cost = cfg.settlementCost(); break;
            case "CITY": cost = cfg.cityCost(); break;
        }
        if (cost != null && !cost.isBlank()) {
            var bcCost = new src.model.BasicCard(cc.getName());
            bcCost.setCost(cost);
            String msg = rules.explainCostFailure(current, bcCost);
            if (msg != null) { io.out().println(msg); return false; }
            rules.payCost(current, bcCost);
        }
        boolean placed = this.placementRegistry.tryPlace(new src.model.BasicCard(cc.getName()), makeContext(current, opponent));
        if (placed) {
            io.out().println("Played center: " + cc.getName());
            // After a successful center placement, show updated board + hand
            printBoardAndHand(current, io.out());
        }
        return placed;
    }

    private boolean placeRegionFromCenter(Player current, Player opponent){
        var io = ioFor(current);
        // consume from region stack first
        var top = decks.drawRegionCard();
        if (top == null) { io.out().println("No regions left in center stack."); return false; }
        // Ask which region type
        io.out().println("Region type to place: forest, hill, field, pasture, mountain, gold field");
        String t = io.in().readLine(); if (t==null) return false;
        t = t.trim().toLowerCase();
        // Create a pseudo BasicCard by name so RegionPlacementHandler can infer resource
        String name;
        switch (t){
            case "forest": name = "Forest"; break;
            case "hill": name = "Hill"; break;
            case "field": name = "Field"; break;
            case "pasture": name = "Pasture"; break;
            case "mountain": name = "Mountain"; break;
            case "gold field": name = "Gold Field"; break;
            default: io.out().println("Unknown region type."); return false;
        }

        var bc = new src.model.BasicCard(name);
        // Let RegionPlacementHandler handle die assignment and placement validation
        boolean placed = this.placementRegistry.tryPlace(bc, makeContext(current, opponent));
        if (placed){
            io.out().println("Placed region: " + name);
            printBoardAndHand(current, io.out());
        }
        return placed;
    }

    private static src.model.Resource mapRegion(String name){
        String n = name.toLowerCase();
        if (n.contains("forest")) return src.model.Resource.WOOD;
        if (n.contains("hill")) return src.model.Resource.BRICK;
        if (n.equals("field")) return src.model.Resource.WHEAT;
        if (n.contains("pasture")) return src.model.Resource.WOOL;
        if (n.contains("mountain")) return src.model.Resource.ORE;
        if (n.contains("gold field")) return src.model.Resource.GOLD;
        return src.model.Resource.WOOD;
    }

    public void replenish(Player p){
        var io = ioFor(p);
        decks.replenishHand(p, io.in(), io.out());
    }
    public void exchange(Player p){
        var io = ioFor(p);
        decks.optionalExchange(p, io.in(), io.out());
    }
    public int peekEvent(){ return rng.nextInt(6)+1; }
    public void declareWinner(Player w){ out.println("Winner: " + w.getName()); }

    public src.io.interfaces.IInputService getInput(){ return in; }
    public src.io.interfaces.IOutputService getOutput(){ return out; }
    public RuleValidator getRules(){ return rules; }
    public DeckManager getDecks(){ return decks; }

    // ===== Storage-only helpers for trades =====
    private src.model.Resource parseResource(String s){
        if (s == null) return null;
        String t = s.trim().toLowerCase();
        switch (t){
            case "brick": return src.model.Resource.BRICK;
            case "grain":
            case "wheat": return src.model.Resource.WHEAT;
            case "lumber":
            case "wood": return src.model.Resource.WOOD;
            case "wool": return src.model.Resource.WOOL;
            case "ore": return src.model.Resource.ORE;
            case "gold": return src.model.Resource.GOLD;
            default: return null;
        }
    }

    private int totalStored(Player p, src.model.Resource r){
        int s = 0;
        for (var t : p.principality().regions()) if (t.getResource() == r) s += t.getStored();
        return s;
    }

    private boolean hasCapacityFor(Player p, src.model.Resource r, int add){
        int need = add;
        for (var t : p.principality().regions()) {
            if (t.getResource() != r) continue;
            int cap = Math.max(0, 3 - t.getStored());
            if (cap <= 0) continue;
            need -= cap;
            if (need <= 0) return true;
        }
        return need <= 0;
    }

    private void consumeStored(Player p, src.model.Resource r, int amount){
        int need = amount;
        for (var t : p.principality().regions()) {
            if (need == 0) break;
            if (t.getResource() != r) continue;
            int s = t.getStored();
            if (s <= 0) continue;
            int take = Math.min(s, need);
            t.setStored(s - take);
            need -= take;
        }
    }

    private void addStored(Player p, src.model.Resource r, int amount){
        int need = amount;
        for (var t : p.principality().regions()) {
            if (need == 0) break;
            if (t.getResource() != r) continue;
            int cap = Math.max(0, 3 - t.getStored());
            if (cap <= 0) continue;
            int addv = Math.min(cap, need);
            t.setStored(t.getStored() + addv);
            need -= addv;
        }
    }

    // Friendly resource name helpers for messages
    private String friendly(src.model.Resource r){
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
    private String friendlyList(java.util.EnumSet<src.model.Resource> set){
        java.util.ArrayList<String> names = new java.util.ArrayList<>();
        for (var r : set) names.add(friendly(r));
        return String.join(", ", names);
    }

    // ===== UI helpers =====
    private String badge(String value, String label){
        if (value == null || value.isBlank()) return "";
        return "  {" + (label != null? label + ": ": "") + value + "}";
    }

    /** Quick inference for points badges from known card names. Extend with more mapping if needed. */
    public static String summarizePoints(src.model.BasicCard c){
        if (c == null || c.getName() == null) return "";
        String n = c.getName().trim().toLowerCase();
        // Abbey: +1 PP
        if (n.equals("abbey")) return "PP1";
        // Common heroes mapped in EffectCatalog:
        if (n.equals("austin")) return "SP1 FP2";
        if (n.equals("candamir")) return "SP4 FP1";
        if (n.equals("harald")) return "SP2 FP1";
        if (n.equals("inga")) return "SP1 FP3";
        if (n.equals("osmund")) return "SP2 FP2";
        if (n.equals("siglind")) return "SP2 FP3";
        // Common trade ships and Large Trade Ship: +1 CP on placement
        if (n.equals("lumber ship")) return "CP1";
        if (n.equals("brick ship")) return "CP1";
        if (n.equals("grain ship")) return "CP1";
        if (n.equals("ore ship")) return "CP1";
        if (n.equals("wool ship")) return "CP1";
        if (n.equals("gold ship")) return "CP1";
        if (n.equals("large trade ship")) return "CP1";
        // Toll Bridge grants +1 CP on placement in this ruleset
        if (n.equals("toll bridge")) return "CP1";
        // Marketplace grants +1 CP on placement
        if (n.equals("marketplace")) return "CP1";
        return "";
    }

    /** Print the active player's current board and full hand listing. */
    private void printBoardAndHand(Player p, src.io.interfaces.IOutputService out){
        src.view.BoardPrinter.printPlayerBoard(p, out);
        out.println("Your hand:");
        java.util.List<BasicCard> hand = p.getHandSnapshot();
        out.println("Hand ("+hand.size()+"):");
        for (int i=0;i<hand.size();i++){
            var c = hand.get(i);
            String cost = c.getCost();
            String pts = summarizePoints(c);
            out.println("  ["+i+"] " + c.getName() + badge(cost, "cost") + badge(pts, null));
            if (c.getDescription()!=null && !c.getDescription().isBlank()) out.println("    "+c.getDescription());
        }
    }
}