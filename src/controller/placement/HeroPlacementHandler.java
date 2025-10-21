package src.controller.placement;

import src.controller.GameContext;
import src.model.BasicCard;
import src.model.Principality;
import src.view.BoardPrinter;

/** Handles placement of Common Heroes (Harald, Inga, etc.)
 * Rule: place on any empty building site adjacent to one of your settlements/cities — that is,
 * the slot directly above (row 1) or below (row 3) the left or right settlement/city (col 1 or 3).
 */
public final class HeroPlacementHandler implements PlacementHandler {
    @Override public boolean canHandle(BasicCard card){
        String n = (card.getName()==null?"":card.getName()).trim().toLowerCase();
        // Common hero names per rulebook
        return n.equals("harald") || n.equals("inga") || n.equals("austin")
            || n.equals("candamir") || n.equals("osmund") || n.equals("siglind");
    }

    @Override public boolean place(BasicCard card, GameContext ctx){
        var in = ctx.in(); var out = ctx.out(); var p = ctx.current();
        Principality pr = p.principality();

        out.println("Place HERO ("+card.getName()+"): row 0/1/3/4; use the displayed column number shown in the board header for the settlement column. Type 'cancel' to abort.");
        int[] rc = CoordinatePrompter.askCoord(
            in, out,
            "Enter row col for hero (e.g., '0 1', '1 1', '3 3', '4 3'):",
            (r,c) -> (r==0 || r==1 || r==3 || r==4) && pr.canPlaceBuildingAtDisplay(r, c),
            "Row must be 0, 1, 3, or 4; column must be a valid building site (the settlement column in that row)."
        );
        if (rc == null) { out.println("Hero placement cancelled."); return false; }
        pr.placeBuildingAtDisplay(rc[0], rc[1], card.getName());
        out.println("Placed hero '"+card.getName()+"' at ("+rc[0]+","+rc[1]+").");
        // Apply the hero's effect (e.g., SP/FP) now that it is on the board
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
