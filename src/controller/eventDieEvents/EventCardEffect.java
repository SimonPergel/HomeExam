
package src.controller.eventDieEvents;

import src.controller.ICardEffect;
import src.controller.GameContext;
/** Placeholder for event-card-backed effects. */
public class EventCardEffect implements ICardEffect {
    private final String name;
    public EventCardEffect(String name){ this.name=name; }
    @Override public void apply(GameContext ctx){ ctx.out().println("Event card: "+name+" (resolve per card text)"); }
    @Override public String description(){ return "EventCard("+name+")"; }
}
