package src.controller.placement;

import src.controller.GameContext;
import src.model.BasicCard;
import src.model.Principality;
import src.view.BoardPrinter;

/** Handles placement for generic settlement/city expansion buildings:
 *  - Marketplace (max 1)
 *  - Storehouse
 *  - Toll Bridge
 *  - Production boosters: Brick Factory, Grain Mill, Iron Foundry, Lumber Camp, Weaver's Shop
 */
public final class GenericBuildingPlacementHandler implements PlacementHandler {
    private static final String[] NAMES = new String[]{
        // Buildings
        "marketplace", "storehouse", "toll bridge", "tool brige",
        "brick factory", "grain mill", "iron foundry", "lumber camp",
        "weaver's shop", "weavers shop", "weaver’s shop", // tolerate apostrophes/typos
        // Trade ships (units)
        "lumber ship", "brick ship", "grain ship", "ore ship", "wool ship", "gold ship",
        "large trade ship"
    };

    @Override public boolean canHandle(BasicCard card){
        String n = normalize(card.getName());
        for (String s : NAMES) if (n.equals(s)) return true;
        return false;
    }

    @Override public boolean place(BasicCard card, GameContext ctx){
        var in = ctx.in(); var out = ctx.out(); var p = ctx.current();
        Principality pr = p.principality();

        String name = (card.getName()==null?"":card.getName()).trim();
        String n = normalize(name);
        if (n.equals("marketplace") && pr.hasBuildingNamed("Marketplace")){
            out.println("You may only have 1 Marketplace in your principality.");
            return false;
        }

        out.println("Place "+name.toUpperCase()+": row 0/1 (top) or 3/4 (bottom), use the displayed column number in the header for the settlement column. Type 'cancel' to abort.");
        int[] rc = CoordinatePrompter.askCoord(
            in, out,
            "Enter row col for "+name+" (e.g., '0 1', '1 1', '3 3', '4 3'):",
            (r,c) -> (r==0 || r==1 || r==3 || r==4) && pr.canPlaceBuildingAtDisplay(r, c),
            "Row must be 0, 1, 3, or 4; column must be a valid building site (the settlement column in that row)."
        );
        if (rc == null) { out.println(name+" placement cancelled."); return false; }
        pr.placeBuildingAtDisplay(rc[0], rc[1], name);
        out.println("Placed "+name+" at ("+rc[0]+","+rc[1]+").");
    // Apply card effect on placement if any (e.g., trade ships grant 2:1 + CP, Abbey PP handled elsewhere)
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

    private static String normalize(String s){
        if (s == null) return "";
        // Replace curly apostrophe with straight for robust matching
        return s.trim().toLowerCase().replace('\u2019', '\'').replace("’", "'");
    }
}
