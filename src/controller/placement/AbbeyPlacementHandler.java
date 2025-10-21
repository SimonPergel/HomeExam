package src.controller.placement;

import src.controller.GameContext;
import src.model.BasicCard;
import src.model.Principality;
import src.view.BoardPrinter;

/** Handles placement of Abbey as a building: must be on an empty building site adjacent to a settlement/city. */
public final class AbbeyPlacementHandler implements PlacementHandler {
    @Override public boolean canHandle(BasicCard card){
        String n = (card.getName()==null?"":card.getName()).trim().toLowerCase();
        return n.equals("abbey");
    }

    @Override public boolean place(BasicCard card, GameContext ctx){
        var in = ctx.in(); var out = ctx.out(); var p = ctx.current();
        Principality pr = p.principality();

        out.println("Place ABBEY: row 0/1 (top) or 3/4 (bottom), choose the displayed column for ANY settlement column (inner or outer). Type 'cancel' to abort.");
        int[] rc = CoordinatePrompter.askCoord(
            in, out,
            "Enter row col for Abbey (e.g., '0 1', '1 1', '3 3', '4 3'):",
            (r,c) -> (r==0 || r==1 || r==3 || r==4) && pr.canPlaceBuildingAtDisplay(r, c),
            "Row must be 0, 1, 3, or 4; column must be a valid building site (the settlement column in that row)."
        );
        if (rc == null) { out.println("Abbey placement cancelled."); return false; }

        if (!pr.canPlaceBuildingAtDisplay(rc[0], rc[1])){
            out.println("Illegal Abbey placement at ("+rc[0]+","+rc[1]+"). Requires adjacent settlement/city and empty site.");
            return false;
        }
        pr.placeBuildingAtDisplay(rc[0], rc[1], card.getName());
        out.println("Placed Abbey at ("+rc[0]+","+rc[1]+").");
        // Apply effect (adds +1 PP) immediately after placement
        if (card.getEffect() != null) card.getEffect().apply(ctx);
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
