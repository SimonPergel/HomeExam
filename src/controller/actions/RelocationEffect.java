package src.controller.actions;

import src.controller.GameContext;
import src.controller.ICardEffect;

public class RelocationEffect implements ICardEffect {
    @Override public void apply(GameContext ctx) {
        var p = ctx.current();
        var in = ctx.in();
        var out = ctx.out();

        int n = p.principality().regions().size();
        if (n < 2) {
            out.println("Relocation: need at least 2 regions.");
            return;
        }

        out.println("Relocation: enter TWO region indices to swap their die numbers (e.g., '0 3').");
        int i = readIndex(in, out, n, "first index");
        int j = readIndex(in, out, n, "second index");
        if (i == j) { out.println("Relocation: same indices; nothing changed."); return; }
        p.principality().swapRegionDice(i, j);
        out.println("Relocation: swapped dice of regions ["+i+"] and ["+j+"].");
    }

    private int readIndex(src.io.interfaces.IInputService in,
                          src.io.interfaces.IOutputService out,
                          int size, String label){
        while (true){
            out.println("Enter " + label + " (0.." + (size-1) + "):");
            try {
                String s = in.readLine();
                int v = Integer.parseInt(s.trim());
                if (v>=0 && v<size) return v;
            } catch(Exception ignore){}
            out.println("Invalid index.");
        }
    }
}
