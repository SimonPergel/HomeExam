package src.controller.placement;

import src.controller.GameContext;
import src.model.BasicCard;
import src.model.Principality;
import src.view.BoardPrinter;

public final class CityPlacementHandler implements PlacementHandler {
    @Override
    public boolean canHandle(BasicCard card) {
        String n = (card.getName()==null?"":card.getName()).trim().toLowerCase();
        return n.equals("city");
    }

    @Override
    public boolean place(BasicCard card, GameContext ctx) {
        var in  = ctx.in();
        var out = ctx.out();
        var p   = ctx.current();
        Principality princ = p.principality();

        int[] rc = CoordinatePrompter.askCoord(
            in, out,
            "Upgrade to CITY. Row must be 2. Choose the displayed column of ANY settlement (inner or outer). Enter row col (or 'cancel'):",
            (r,c) -> r == 2 && (c >= 0 && c < princ.width()),
            "Row must be 2 and a valid display column."
        );
        if (rc == null) { 
            out.println("City upgrade cancelled."); 
            return false; }

        int displayCol = rc[1];
        // Map to any settlement column (supports inner and outer)
        int siteCol = princ.mapDisplayColToAnySettlementCol(displayCol);
        if (siteCol < 0) {
            out.println("Invalid coordinate: " + rc[0] + " " + rc[1] + ". Row must be 2; choose a column that currently has your settlement.");
            return false;
        }
        if (!princ.canPlaceCityAt(2, siteCol)) {
            out.println("Illegal city upgrade at (2," + displayCol + ") — requires an existing settlement there and not already a city.");
            return false;
        }

        // Perform the upgrade on the board state (unlocks 2 additional building sites for inner city)
        princ.upgradeCityAt(2, siteCol);
        // Victory Points: settlement was 1 VP; city is 2 VP -> net +1 VP on upgrade
        princ.getPoints().addVP(1);
        // Return the consumed settlement to the bottom of the settlement stack (optional parity with center stacks)
        try { ctx.decks().returnSettlementToBottom(); } catch (Exception ignore) {}
        out.println("Upgraded to City at (2," + displayCol + ").");
        BoardPrinter.printPlayerBoard(p, out);
        out.println("Your hand:");
        java.util.List<src.model.BasicCard> hand = p.getHandSnapshot();
        out.println("Hand ("+hand.size()+"):");
        for (int i=0;i<hand.size();i++){
            var c = hand.get(i);
            String cost = c.getCost();
            if (cost != null && !cost.isBlank()) cost = src.util.CostParser.legacyToNewLetters(cost);
            String pts = src.controller.GameController.summarizePoints(c);
            out.println("  ["+i+"] " + c.getName() + (cost!=null && !cost.isBlank()? ("  {cost: "+cost+"}"): "") + (pts.isBlank()?"":"  {"+pts+"}"));
            if (c.getDescription()!=null && !c.getDescription().isBlank()) out.println("    "+c.getDescription());
        }
        return true;
    }
}