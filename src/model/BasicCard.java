package src.model;

import src.controller.ICardEffect;

// this class implements a read ability for the cost filed from card.json and inforce it when playing

public class BasicCard extends Card {
    private ICardEffect effect; // Strategy
    private String cost;        // e.g. "BGO", "WO", may be null/empty
    private String description; // optional card text from JSON

    public BasicCard(String name) { super(name); }

    public BasicCard(String name, ICardEffect effect) {
        super(name);
        this.effect = effect;
    }

    public ICardEffect getEffect() { return effect; }
    public void setEffect(ICardEffect effect) { this.effect = effect; }

    public String getCost() { return cost; }
    public void setCost(String cost) { this.cost = cost; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}