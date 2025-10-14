package src.model;

import java.util.*;

public class Deck {
    private List<Card> cards = new ArrayList<>();
    public Deck() {}
    public Deck(List<Card> cards) { this.cards.addAll(cards); }
    public boolean isEmpty() { return cards.isEmpty(); }
    public Card drawTop() { return cards.isEmpty() ? null : cards.remove(0); }
    public void pushBottom(Card c) { if (c != null) cards.add(c); }
    public void shuffle(Random rnd) { Collections.shuffle(cards, rnd); }
    public int size() { return cards.size(); }
}
