package src.controller.placement;

import java.util.List;
import src.controller.GameContext;
import src.model.BasicCard;

public final class PlacementRegistry {
    private final List<PlacementHandler> handlers;

    public PlacementRegistry(List<PlacementHandler> handlers) {
        this.handlers = List.copyOf(handlers);
    }

    /** Return true if any handler placed it. */
    public boolean tryPlace(BasicCard card, GameContext ctx) {
        for (var h : handlers) {
            if (h.canHandle(card)) {
                return h.place(card, ctx);
            }
        }
        return false;
    }

    /** Expose whether any handler can handle this card (useful for refund-on-cancel logic). */
    public boolean canHandle(BasicCard card) {
        for (var h : handlers) {
            if (h.canHandle(card)) return true;
        }
        return false;
    }
}
