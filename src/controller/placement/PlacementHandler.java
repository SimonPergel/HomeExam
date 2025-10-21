package src.controller.placement;

import src.controller.GameContext;
import src.model.BasicCard;

public interface PlacementHandler {
    /** True if this handler knows how to place this card. */
    boolean canHandle(BasicCard card);

    /** Perform placement. Return true if placed on the board; false if not (e.g., cancel/illegal). */
    boolean place(BasicCard card, GameContext ctx);
}