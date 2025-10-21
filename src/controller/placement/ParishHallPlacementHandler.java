package src.controller.placement;

import src.controller.GameContext;
import src.model.BasicCard;
import src.model.Principality;
import src.view.BoardPrinter;

/** Handles placement of Parish Hall: one per principality, on an empty building site adjacent to a settlement/city. */
public final class ParishHallPlacementHandler implements PlacementHandler {
    @Override public boolean canHandle(BasicCard card){
        String n = (card.getName()==null?"":card.getName()).trim().toLowerCase();
        return n.equals("parish hall");
    }

    @Override public boolean place(BasicCard card, GameContext ctx){
        var in = ctx.in(); var out = ctx.out(); var p = ctx.current();
        Principality pr = p.principality();

        if (pr.hasParishHall()){
            out.println("You may only have 1 Parish Hall in your principality.");
            return false;
        }

        out.println("Place PARISH HALL: row 1 (top) or 3 (bottom), column 1 (above/below left) or 3 (above/below right). Type 'cancel' to abort.");
        int[] rc = CoordinatePrompter.askCoord(
            in, out,
            "Enter row col for Parish Hall (e.g., '1 1', '3 3'):",
            (r,c) -> (r==1 || r==3) && (c==1 || c==3),
            "Row must be 1 or 3; column must be 1 or 3."
        );
        if (rc == null) { out.println("Parish Hall placement cancelled."); return false; }

        if (!pr.canPlaceBuildingAt(rc[0], rc[1])){
            out.println("Illegal Parish Hall placement at ("+rc[0]+","+rc[1]+"). Requires adjacent settlement/city and empty site.");
            return false;
        }
        pr.placeBuildingAt(rc[0], rc[1], card.getName());
        out.println("Placed Parish Hall at ("+rc[0]+","+rc[1]+"). You now pay only 1 resource when searching a draw stack during Exchange.");
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
