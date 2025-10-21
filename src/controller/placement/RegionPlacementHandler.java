package src.controller.placement;

import java.util.Locale;
import src.controller.GameContext;
import src.model.BasicCard;
import src.model.Principality;
import src.model.RegionTile;
import src.model.Resource;
import src.view.BoardPrinter;

public final class RegionPlacementHandler implements PlacementHandler {
    @Override public boolean canHandle(BasicCard card){
        String n = (card.getName()==null?"":card.getName()).trim().toLowerCase();
        return n.contains("forest") || n.contains("hill") || n.contains("field")
            || n.contains("pasture") || n.contains("mountain") || n.contains("gold field");
    }

    @Override public boolean place(BasicCard card, GameContext ctx){
        var in = ctx.in(); var out = ctx.out(); var p = ctx.current();
        Principality pr = p.principality();

        Resource res = inferRegionResource(card.getName());
        if (res == null) { out.println("Unknown region type for: " + card.getName()); return false; }

        int[] rc = CoordinatePrompter.askCoord(
            in, out,
            "Place REGION (" + card.getName() + "). Row 1 (top) or 3 (bottom). Enter row col (or 'cancel'):",
            (r,c) -> (r == 1 || r == 3),
            "Row must be 1 or 3."
        );
        if (rc == null) { out.println("Region placement cancelled."); return false; }

        // Auto-assign die from DeckManager based on resource per requirement 2.c
    Integer die = ctx.decks().nextAssignedDieFor(res).orElseGet(() -> 1 + new java.util.Random().nextInt(6));
        RegionTile tile = new RegionTile(res, die);
        try {
            if (!pr.canPlaceRegionAt(rc[0], rc[1])) {
                out.println("Illegal region placement at ("+rc[0]+","+rc[1]+").");
                return false;
            }
            pr.placeRegionAt(rc[0], rc[1], tile);
            out.println("Placed " + card.getName() + " at (" + rc[0] + "," + rc[1] + ") with d" + die + ".");
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
        } catch (Exception ex) {
            out.println("Illegal region placement: " + ex.getMessage());
            return false;
        }
    }

    private static Resource inferRegionResource(String name) {
        if (name == null) return null;
        String n = name.trim().toLowerCase(Locale.ROOT);
        if (n.contains("forest")) return Resource.WOOD;
        if (n.contains("hill")) return Resource.BRICK;
        if (n.contains("field") && !n.contains("gold")) return Resource.WHEAT;
        if (n.contains("pasture")) return Resource.WOOL;
        if (n.contains("mountain")) return Resource.ORE;
        if (n.contains("gold field")) return Resource.GOLD;
        return null;
    }
}