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
            "Place ROAD. Row must be 2. Choose any empty column. Road must be adjacent to a settlement or city.\n"+
            "Enter row col (or 'cancel'):",
            (r,c) -> r == 2,
            "Row must be 2 and column must be a valid board column."
        );
        if (rc == null) { out.println("Road placement cancelled."); return false; }
        if (!pr.canPlaceRoadAt(rc[0], rc[1])) { out.println("Illegal road placement at ("+rc[0]+","+rc[1]+") — must be empty and adjacent to a settlement or city."); return false; }
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