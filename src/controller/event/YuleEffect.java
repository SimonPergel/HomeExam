package src.controller.event;

import src.controller.GameContext;
import src.controller.ICardEffect;
import src.controller.eventDieEvents.EventCardEffect;


/**
 * Yule (1):
 * If the Yule event card is revealed, prepare a new event card stack; then draw a new event card.
 *
 * NOTE: Your setup already places Yule 4th from bottom. Here we draw and resolve
 * a NEW event card immediately. Building a new event deck structure is handled at setup time,
 * so we only perform the "draw another event" part.
 */
public class YuleEffect implements ICardEffect {
    @Override public void apply(GameContext ctx) {
        ctx.out().println("Event: Yule — Prepare a new event card stack (handled by setup); drawing another event now.");
        var next = ctx.decks().drawEventCard();
        var name = next.getName();
        // Dispatch the next event. Fallback to a generic wrapper here to avoid wider dependencies.
        new EventCardEffect(name).apply(ctx);
    }
}
