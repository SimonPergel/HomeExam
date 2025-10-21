package src.controller.placement;

import src.controller.GameContext;
import src.model.BasicCard;
import src.model.Principality;
import src.view.BoardPrinter;

public final class RoadPlacementHandler implements PlacementHandler {
    @Override public boolean canHandle(BasicCard card){
        String n = (card.getName()==null?"":card.getName()).trim().toLowerCase();
        return n.equals("road");
    }

    @Override public boolean place(BasicCard card, GameContext ctx){
        var in = ctx.in(); var out = ctx.out(); var p = ctx.current();
        Principality pr = p.principality();

        int[] rc = CoordinatePrompter.askCoord(
            in, out,
            "Place ROAD. Row must be 2. Allowed columns: 0 (left edge), 2 (center), 4 (right edge).\n"+
            "Starter board example: (2 0) left extension, (2 2) center, (2 4) right extension. Enter row col (or 'cancel'):",
            (r,c) -> r == 2 && (c == 0 || c == 2 || c == 4),
            "Row must be 2 and column must be 0, 2, or 4."
        );
        if (rc == null) { out.println("Road placement cancelled."); return false; }
    if (!pr.canPlaceRoadAt(rc[0], rc[1])) { out.println("Illegal road placement at ("+rc[0]+","+rc[1]+") — row must be 2 and slot must be empty."); return false; }
        pr.placeRoadAt(rc[0], rc[1]);
        out.println("Placed Road at ("+rc[0]+","+rc[1]+").");
        BoardPrinter.printPlayerBoard(p, out);
        // Also show current hand after the action
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