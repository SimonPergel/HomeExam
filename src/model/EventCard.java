
package src.model;
import src.controller.ICardEffect;
public class EventCard extends Card {
    private ICardEffect effect;
    public EventCard(String name){ super(name); }
    public EventCard(String name, ICardEffect effect){ super(name); this.effect=effect; }
    public ICardEffect getEffect(){ return effect; }
}
