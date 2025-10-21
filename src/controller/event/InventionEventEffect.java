package src.controller.event;

import src.model.*;
import src.controller.GameContext;
import src.controller.ICardEffect;

/** Invention (1): Each player determines which resources to receive and among which regions to distribute them
 * Up to 2 resources total, and must be placed as storage on regions of the chosen type(s) with capacity.
 */
public class InventionEventEffect implements ICardEffect {
    @Override
    public void apply(GameContext ctx) {
        applyFor(ctx.current(), ctx);
        applyFor(ctx.opponent(), ctx);
    }

    private void applyFor(Player p, GameContext ctx) {
        var in = ctx.in();
        var out = ctx.out();

        int grants = Math.min(2, Math.max(0, p.getProgressPoints()));
        if (grants == 0) {
            out.println(p.getName() + " has no progress points — receives nothing.");
            return;
        }
        out.println(p.getName() + ": Invention — choose " + grants + " resource(s) and assign to regions with space.");
        for (int i = 0; i < grants; i++) {
            Resource r = p.chooseResource(in, out);
            // Find regions of this type with capacity
            java.util.ArrayList<Integer> idxs = new java.util.ArrayList<>();
            var regs = p.principality().regions();
            for (int idx=0; idx<regs.size(); idx++){
                var t = regs.get(idx);
                if (t != null && t.getResource() == r && t.getStored() < 3) idxs.add(idx);
            }
            if (idxs.isEmpty()) {
                out.println("No region with space for " + r + "; skipping this unit.");
                continue;
            }
            int choice = 0;
            if (idxs.size() > 1){
                out.println("Choose region index to store +1 " + r + ":");
                for (int j=0;j<idxs.size();j++){
                    int k = idxs.get(j);
                    out.println("  " + (j+1) + ") region#"+k+" stored="+regs.get(k).getStored());
                }
                choice = readIndex(in, out, "Enter 1.."+idxs.size()+":", 1, idxs.size()) - 1;
            }
            var tile = regs.get(idxs.get(choice));
            tile.setStored(tile.getStored()+1);
        }
    }

    private int readIndex(src.io.interfaces.IInputService in, src.io.interfaces.IOutputService out, String prompt, int lo, int hi){
        while(true){
            out.println(prompt);
            try{
                int v = Integer.parseInt(in.readLine().trim());
                if (v>=lo && v<=hi) return v;
            }catch(Exception ignore){}
            out.println("Please enter a number between "+lo+" and "+hi+".");
        }
    }
}