package src.model;

public class EventCard extends Card {
    public EventCard(String name, String text) {
        super();
        this.name = name;
        this.type = "Event";
        this.cardText = text;
        this.placement = "Event";
    }
}
