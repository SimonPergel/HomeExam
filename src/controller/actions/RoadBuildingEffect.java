package src.controller.actions;

import src.controller.GameContext;
import src.controller.ICardEffect;
import src.controller.placement.CoordinatePrompter;
import src.model.Principality;
import src.view.BoardPrinter;

/** Event card: Road Building — place up to two roads. */
public final class RoadBuildingEffect implements ICardEffect {
    @Override
    public void apply(GameContext ctx) {
        var out = ctx.out();
        var in  = ctx.in();
        var p   = ctx.current();
        Principality princ = p.principality();

        out.println("Road Building: you may place up to TWO roads.");

        int placed = 0;
        for (int i = 0; i < 2; i++) {
            int[] rc = CoordinatePrompter.askCoord(
                in, out,
                "Place ROAD #" + (i+1) + ". Row must be 2. Allowed columns: 0 (left edge), 2 (center), 4 (right edge). Enter row col (or 'cancel'):",
                (r,c) -> r == 2 && (c == 0 || c == 2 || c == 4),
                "Row must be 2 and column must be 0, 2, or 4."
            );
            if (rc == null) {
                out.println("Road #" + (i+1) + " cancelled.");
                continue;
            }
            if (!princ.canPlaceRoadAt(rc[0], rc[1])) {
                out.println("Illegal road placement at (" + rc[0] + "," + rc[1] + ") — needs adjacent settlement for edge roads and empty slot.");
                continue;
            }
            princ.placeRoadAt(rc[0], rc[1]);
            out.println("Placed Road at (" + rc[0] + "," + rc[1] + ").");
            BoardPrinter.printPlayerBoard(p, out);
            placed++;
        }

        if (placed == 0) {
            out.println("Road Building: no roads placed.");
        } else {
            out.println("Road Building: placed " + placed + " road" + (placed == 1 ? "" : "s") + ".");
        }
    }
}
