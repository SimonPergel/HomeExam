package src.model;

public final class Points {
    private int vp, cp, sp, fp, pp;
    public int getVP(){ return vp; }   public void addVP(int d){ vp = Math.max(0, vp + d); }
    public int getCP(){ return cp; }   public void addCP(int d){ cp = Math.max(0, cp + d); }
    public int getSP(){ return sp; }   public void addSP(int d){ sp = Math.max(0, sp + d); }
    public int getFP(){ return fp; }   public void addFP(int d){ fp = Math.max(0, fp + d); }
    public int getPP(){ return pp; }   public void addPP(int d){ pp = Math.max(0, pp + d); }
}
