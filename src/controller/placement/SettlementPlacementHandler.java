package src.controller.placement;

import src.controller.GameContext;
import src.model.BasicCard;
import src.model.Principality;
import src.view.BoardPrinter;

public final class SettlementPlacementHandler implements PlacementHandler {
    @Override
    public boolean canHandle(BasicCard card) {
        String n = (card.getName()==null?"":card.getName()).trim().toLowerCase();
        return n.equals("settlement");
    }

    @Override
    public boolean place(BasicCard card, GameContext ctx) {
        var in  = ctx.in();
        var out = ctx.out();
        var p   = ctx.current();
        Principality princ = p.principality();

        int[] rc = CoordinatePrompter.askCoord(
            in, out,
            "Place SETTLEMENT. Row must be 2. Valid: (2 1) left, (2 3) right, (2 0) outer-left (with left road), or (2 <lastCol>) outer-right (with right road).\n"+
            "Rule: To extend with a new settlement, you must have a road adjacent to that side: (2,0) for left or (2,4) for right.\n"+
            "If you place at the outer-left/right column next to the corresponding edge road, the board will extend and 2 regions (top/bottom) will be drawn and placed in the newly added column. Enter row col (or 'cancel'):",
            (r,c) -> r == 2, // accept row 2, any column; we'll validate precisely below
            "Row must be 2."
        );
        if (rc == null) { 
            out.println("Settlement placement cancelled."); 
            return false; }

        // Board-level allows seeding; enforce the rulebook adjacency here for player actions.
        if (rc[0] != 2) {
            out.println("Illegal settlement placement: row must be 2.");
            return false;
        }
        if (!princ.canPlaceSettlementAt(rc[0], rc[1])) {
            out.println("Illegal settlement placement at (" + rc[0] + "," + rc[1] + ").");
            return false;
        }
        if (rc[1] == 1 && !princ.hasRoadLeft()) {
            out.println("Illegal settlement placement: requires adjacent left road at (2,0).");
            return false;
        }
        if (rc[1] == 3 && !princ.hasRoadRight()) {
            out.println("Illegal settlement placement: requires adjacent right road at (2,4).");
            return false;
        }

        // Outer-edge special cases: allow placing at first/last column iff corresponding edge road exists.
        boolean outerLeftCase = (rc[1] == 0);
        int lastCol = princ.width() - 1;
        boolean outerRightCase = (rc[1] == lastCol);
        if (outerRightCase) {
            if (!princ.hasRoadRight()) {
                out.println("Outer-right settlement requires a right edge road at (2,4).");
                return false;
            }
        }
        if (outerLeftCase) {
            if (!princ.hasRoadLeft()) {
                out.println("Outer-left settlement requires a left edge road at (2,0).");
                return false;
            }
        }

        princ.placeSettlementAt(rc[0], rc[1]);
        // Each settlement is worth 1 VP
        princ.getPoints().addVP(1);
        out.println("Placed Settlement at (" + rc[0] + "," + rc[1] + ").");

        // If outer-right settlement, auto-place 2 regions above and below from the region stack
        if (outerRightCase) {
            try {
                // After placing the outer-right settlement, the board extended by 1 column.
                // Place the regions in that newly added far-right column (last index).
                int colForRegions = Math.max(0, princ.width() - 1);
                // Draw two region cards from center stack via DeckManager
                var dm = ctx.decks();
                var topCard = dm.drawRegionCard();
                var bottomCard = dm.drawRegionCard();
                if (topCard != null) {
                    // Infer resource from card name, assign die via pool
                    src.model.Resource resTop = inferRegionResource(topCard.getName());
                    int dieTop = dm.nextAssignedDieFor(resTop).orElse(1);
                    var tileTop = new src.model.RegionTile(resTop, dieTop);
                    princ.placeRegionAt(1, colForRegions, tileTop);
                }
                if (bottomCard != null) {
                    src.model.Resource resBot = inferRegionResource(bottomCard.getName());
                    int dieBot = dm.nextAssignedDieFor(resBot).orElse(1);
                    var tileBot = new src.model.RegionTile(resBot, dieBot);
                    princ.placeRegionAt(3, colForRegions, tileBot);
                }
                out.println("Auto-placed new regions at (1,"+colForRegions+") and (3,"+colForRegions+") in the newly added column.");
            } catch (Exception ex) {
                out.println("Warning: failed to auto-place regions: " + ex.getMessage());
            }
        }
        if (outerLeftCase) {
            try {
                // After placing the outer-left settlement, the board extended by 1 column on the left.
                // The newly added column sits at index 0.
                int colForRegions = 0;
                var dm = ctx.decks();
                var topCard = dm.drawRegionCard();
                var bottomCard = dm.drawRegionCard();
                if (topCard != null) {
                    src.model.Resource resTop = inferRegionResource(topCard.getName());
                    int dieTop = dm.nextAssignedDieFor(resTop).orElse(1);
                    princ.placeRegionAt(1, colForRegions, new src.model.RegionTile(resTop, dieTop));
                }
                if (bottomCard != null) {
                    src.model.Resource resBot = inferRegionResource(bottomCard.getName());
                    int dieBot = dm.nextAssignedDieFor(resBot).orElse(1);
                    princ.placeRegionAt(3, colForRegions, new src.model.RegionTile(resBot, dieBot));
                }
                out.println("Auto-placed new regions at (1,"+colForRegions+") and (3,"+colForRegions+") in the newly added column.");
            } catch (Exception ex) {
                out.println("Warning: failed to auto-place regions: " + ex.getMessage());
            }
        }
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

    // Duplicate minimal infer function here to avoid coupling
    private static src.model.Resource inferRegionResource(String name) {
        if (name == null) return src.model.Resource.WOOD;
        String n = name.trim().toLowerCase();
        if (n.contains("forest")) return src.model.Resource.WOOD;
        if (n.contains("hill")) return src.model.Resource.BRICK;
        if (n.contains("gold field")) return src.model.Resource.GOLD;
        if (n.contains("field")) return src.model.Resource.WHEAT;
        if (n.contains("pasture")) return src.model.Resource.WOOL;
        if (n.contains("mountain")) return src.model.Resource.ORE;
        return src.model.Resource.WOOD;
    }
}