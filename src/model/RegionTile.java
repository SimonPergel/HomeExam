package src.model;

public class RegionTile {
    private final Resource resource;
    private int die;      // 1..6
    private int stored;   // 0..3

    public RegionTile(Resource resource, int die){
        if (die < 1 || die > 6) throw new IllegalArgumentException("die must be 1..6");
        this.resource = resource;
        this.die = die;
        this.stored = 0; // start empty
    }

    public Resource getResource(){ return resource; }
    public int getDie(){ return die; }
    public void setDie(int die){
        if (die < 1 || die > 6) throw new IllegalArgumentException("die must be 1..6");
        this.die = die;
    }

    // storage (0..3)
    public int getStored() { return stored; }
    public void setStored(int v) { stored = (v < 0 ? 0 : (v > 3 ? 3 : v)); }
    public void incStored() { if (stored < 3) stored++; }
}
