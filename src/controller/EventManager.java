package src.controller;

import src.controller.actions.*;
import src.util.Dice;
import java.util.Locale;
import src.controller.eventDieEvents.EventCardEffect;

public class EventManager {
    public int rollFace(){ return eventDie.roll(6); }
    public void resolveFace(GameContext ctx, DeckManager decks, int roll){
        if (roll >= 1 && roll <= 4) {
            effects.forKey(String.valueOf(roll)).apply(ctx);
            // Re-evaluate advantages after resolving face effect
            src.controller.AdvantageManager.updateAll(ctx);
            return;
        }
    var ec = decks.drawEventCard();
    ctx.out().println("[Event] Draw Event Card");
    ctx.outOpponent().println("[Event] Draw Event Card");
    ctx.out().println("EVENT: " + ec.getName());
    ctx.outOpponent().println("EVENT: " + ec.getName());
        String eventName = ec.getName().toLowerCase(java.util.Locale.ROOT);
        var eff = effects.tryGet("event:" + eventName)
            .orElse(ec.getEffect() != null ? ec.getEffect() : new src.controller.eventDieEvents.EventCardEffect(ec.getName()));
        eff.apply(ctx);
        src.controller.AdvantageManager.updateAll(ctx);
    }

    private final Dice eventDie;
    private final EffectCatalog effects;

    public EventManager(Dice die, EffectCatalog effects){
        this.eventDie = die;
        this.effects = effects;
    }

    public int lastRoll = 0;

    public void resolve(GameContext ctx, DeckManager decks){
        int roll = eventDie.roll(6);
        lastRoll = roll;

        if (roll >= 1 && roll <= 4) {
            effects.forKey(String.valueOf(roll)).apply(ctx);
            src.controller.AdvantageManager.updateAll(ctx);
            return;
        }

        // 5 or 6 → draw an event card and resolve via key (fallback to generic EventCardEffect)
    var ec = decks.drawEventCard();
    ctx.out().println("[Event] Draw Event Card");
    ctx.outOpponent().println("[Event] Draw Event Card");
    ctx.out().println("EVENT: " + ec.getName());
    ctx.outOpponent().println("EVENT: " + ec.getName());
        String eventName = ec.getName().toLowerCase(Locale.ROOT);
        var eff = effects.tryGet("event:" + eventName)
            .orElse(ec.getEffect() != null ? ec.getEffect() : new EventCardEffect(ec.getName()));
        eff.apply(ctx);
        src.controller.AdvantageManager.updateAll(ctx);
        // keep whatever discard/return logic you already use
    }
}
