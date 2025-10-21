package src.controller.event;

import src.controller.GameContext;
import src.controller.ICardEffect;
import src.model.Principality;
import src.model.RegionTile;

/**
 * Year of Plenty (2):
 * If Abbeys and/or Storehouses are adjacent to a single region,
 * that region gets +1 resource for each adjacent building (up to storage limit).
 */
public class YearOfPlentyEffect implements ICardEffect {
    @Override public void apply(GameContext ctx) {
        applyFor(ctx.current(), ctx);
        applyFor(ctx.opponent(), ctx);
    }

    private void applyFor(src.model.Player p, GameContext ctx){
        var out = ctx.out();
        Principality board = p.principality();
        int granted = 0;
        int width = board.width();
        for (int col = 0; col < width; col++){
            // For each column, check top and bottom regions
            RegionTile top = board.getTopRegion(col);
            RegionTile bot = board.getBottomRegion(col);
            if (top != null){
                int adj = countAdjacentBuildings(board, 1, col);
                granted += grantStorage(top, adj);
            }
            if (bot != null){
                int adj = countAdjacentBuildings(board, 3, col);
                granted += grantStorage(bot, adj);
            }
        }
        out.println("Year of Plenty: "+p.getName()+" gains total +"+granted+" stored across regions (subject to storage cap).");
    }

    private int countAdjacentBuildings(Principality board, int regionRow, int col){
        // Adjacent building sites are directly above/below settlements at same column index.
        // Our building sites exist only at columns 1 and 3 for the intro grid. Map region column to nearest settlement column:
        int siteCol = (col <= 2 ? 1 : 3);
        int adj = 0;
        String above = board.getBuildingAt(1, siteCol);
        String below = board.getBuildingAt(3, siteCol);
        if (isAbbeyOrStorehouse(above)) adj++;
        if (isAbbeyOrStorehouse(below)) adj++;
        return adj;
    }

    private boolean isAbbeyOrStorehouse(String name){
        if (name == null) return false;
        String n = name.trim().toLowerCase(java.util.Locale.ROOT);
        return n.equals("abbey") || n.equals("storehouse");
    }

    private int grantStorage(RegionTile tile, int amount){
        if (amount <= 0) return 0;
        int before = tile.getStored();
        int to = Math.min(3, before + amount);
        tile.setStored(to);
        return to - before;
    }
}
