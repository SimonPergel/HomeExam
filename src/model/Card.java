package src.model;

import java.util.*;

/**
 * Minimal Card model adapted from original.
 * Keeps static stacks for simplicity so the rest of the logic can run without a full refactor.
 */
public class Card implements Comparable<Card> {
    public String name, theme, type, placement, cost, oneOf;
    public String victoryPoints, CP, SP, FP, PP, LP, KP, cardText;
    public String germanName, Requires, protectionOrRemoval;
    public int regionProduction = 0;
    public int diceRoll = 0;

    public static Vector<Card> regions = new Vector<>();
    public static Vector<Card> roads = new Vector<>();
    public static Vector<Card> settlements = new Vector<>();
    public static Vector<Card> cities = new Vector<>();
    public static Vector<Card> events = new Vector<>();
    public static Vector<Card> drawStack1 = new Vector<>();
    public static Vector<Card> drawStack2 = new Vector<>();
    public static Vector<Card> drawStack3 = new Vector<>();
    public static Vector<Card> drawStack4 = new Vector<>();

    public Card() {}

    public Card(String name, String theme, String type,
            String germanName, String placement,
            String oneOf, String cost,
            String victoryPoints, String CP, String SP, String FP,
            String PP, String LP, String KP, String Requires,
            String cardText, String protectionOrRemoval) {
        this.name = name;
        this.theme = theme;
        this.type = type;
        this.germanName = germanName;
        this.placement = placement;
        this.oneOf = oneOf;
        this.cost = cost;
        this.victoryPoints = victoryPoints;
        this.CP = CP;
        this.SP = SP;
        this.FP = FP;
        this.PP = PP;
        this.LP = LP;
        this.KP = KP;
        this.Requires = Requires;
        this.cardText = cardText;
        this.protectionOrRemoval = protectionOrRemoval;
    }

    @Override
    public String toString() { return name; }

    @Override
    public int compareTo(Card o) { return this.name.compareToIgnoreCase(o.name); }

    public static Card popCardByName(Vector<Card> cards, String name) {
        if (cards == null || name == null) return null;
        String target = name.trim();
        for (int i = 0; i < cards.size(); i++) {
            Card c = cards.get(i);
            if (c != null && c.name != null && c.name.trim().equalsIgnoreCase(target)) {
                return cards.remove(i);
            }
        }
        return null;
    }

    /**
     * Initialize simple default decks used when no external cards.json is present.
     * This produces region, center, draw and event stacks so the game can run.
     */
    public static void initDefaultDecks() {
        regions.clear(); roads.clear(); settlements.clear(); cities.clear(); events.clear();
        drawStack1.clear(); drawStack2.clear(); drawStack3.clear(); drawStack4.clear();

        // Regions (multiple copies)
        String[] regionNames = {"Forest","Field","Pasture","Hill","Mountain","Gold Field"};
        for (String r : regionNames) {
            for (int i = 0; i < 4; i++)
                regions.add(new Card(r, "basic", "Region", null, null, null, null, null, null, null, null, null, null, null, null, null));
        }

        // Center cards
        for (int i = 0; i < 9; i++) roads.add(new Card("Road","basic","Center",null,"Center",null,"",null,null,null,null,null,null,null,null,null));
        for (int i = 0; i < 9; i++) settlements.add(new Card("Settlement","basic","Center",null,"Center",null,"",null,null,null,null,null,null,null,null,null));
        for (int i = 0; i < 7; i++) cities.add(new Card("City","basic","Center",null,"Center",null,"",null,null,null,null,null,null,null,null,null));

        // Basic draw stacks (generic action cards)
        for (int i = 0; i < 36; i++) {
            Card c = new Card("Action" + i,"basic","Action",null,"Settlement/city",null,"",null,null,null,null,null,null,null,"",null);
            if (i % 4 == 0) drawStack1.add(c);
            else if (i % 4 == 1) drawStack2.add(c);
            else if (i % 4 == 2) drawStack3.add(c);
            else drawStack4.add(c);
        }

        // Events
        events.add(new Card("Feud","basic","Event",null,"Event",null,"",null,null,null,null,null,null,null,"Feud",null));
        events.add(new Card("Year of Plenty","basic","Event",null,"Event",null,"",null,null,null,null,null,null,null,"Year of Plenty",null));
        events.add(new Card("Yule","basic","Event",null,"Event",null,"",null,null,null,null,null,null,null,"Yule",null));
        events.add(new Card("Trade Ships Race","basic","Event",null,"Event",null,"",null,null,null,null,null,null,null,"Trade Ships Race",null));

        // Place Yule 4th from bottom if possible
        Card yule = popCardByName(events, "Yule");
        Collections.shuffle(events);
        if (yule != null && events.size() >= 3) {
            events.add(Math.max(0, events.size() - 3), yule);
        } else if (yule != null) {
            events.add(yule);
        }
    }
}
