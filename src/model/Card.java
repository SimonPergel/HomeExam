
package src.model;

import java.util.Objects;

/** Immutable base type for all cards. */
public class Card implements Comparable<Card> {
    protected final String name;

    public Card(String name) {
        this.name = Objects.requireNonNull(name, "name");
    }

    public String getName() { return name; }

    /** Case-insensitive alphabetical ordering by name. */
    @Override
    public int compareTo(Card o) { return this.name.compareToIgnoreCase(o.name); }

    /** Value semantics: two cards with same name (ignoring case) are equal. */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Card)) return false;
        Card c = (Card) obj;
        return this.name.equalsIgnoreCase(c.name);
    }

    @Override
    public int hashCode() { return name.toLowerCase().hashCode(); }

    @Override
    public String toString() { return name; }
}
