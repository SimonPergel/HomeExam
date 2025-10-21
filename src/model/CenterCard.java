
package src.model;
public class CenterCard extends Card {
    private final String category;
    public CenterCard(String name, String category){ super(name); this.category=category; }
    public String getCategory(){ return category; }
}
