package src.controller.event;

import src.controller.GameContext;
import src.controller.ICardEffect;
import src.model.BasicCard;
import src.model.Principality;

/**
 * Feud (1):
 * - If the affected player has 3 or fewer buildings, they are automatically affected and choose a building to remove.
 * - Otherwise, the player with strength advantage selects 3 of the opponent's buildings. The opponent removes one of them.
 * Removed building returns to the bottom of a matching draw stack (basic stack).
 */
public class FeudEffect implements ICardEffect {
    @Override public void apply(GameContext ctx) {
        var in = ctx.in();
        var out = ctx.out();

        var advantaged = ctx.current();
        var affected = ctx.opponent();
        if (!ctx.rules().hasStrengthAdvantage(advantaged, affected)) {
            if (ctx.rules().hasStrengthAdvantage(affected, advantaged)) {
                var tmp = advantaged; advantaged = affected; affected = tmp;
            } else {
                out.println("Event: Feud — No strength advantage, no effect.");
                return;
            }
        }

        Principality board = affected.principality();
        var occupied = board.getOccupiedBuildingSites();
        if (occupied.isEmpty()) {
            out.println("Feud: Affected player has no buildings to remove.");
            return;
        }

        if (board.countBuildings() <= 3) {
            // Affected chooses any of their buildings to remove
            out.println("Feud: You are affected. Choose one of your buildings to remove:");
            int choice = chooseBuildingIndex(in, out, board);
            int[] rc = occupied.get(choice);
        removeBuildingAtAndReturnToStack(board, rc[0], rc[1], affected, ctx);
        // After potential SP/FP changes from removal, re-evaluate advantages
        src.controller.AdvantageManager.updateAll(ctx);
            return;
        }

        // Advantaged selects up to 3 of opponent's buildings
        int toPick = Math.min(3, occupied.size());
        var uniq = new java.util.LinkedHashSet<String>();
        var chosenCoords = new java.util.ArrayList<int[]>();
        for (int i = 0; i < toPick; i++) {
            int[] rcPick = pickOneFrom(board, occupied, in, out, uniq, i+1);
            chosenCoords.add(rcPick);
        }

        // Opponent chooses one to remove among those selected
        out.println("Feud: Opponent, choose one of these to remove:");
        for (int i=0;i<chosenCoords.size();i++){
            int[] rc2 = chosenCoords.get(i);
            String name = board.getBuildingAt(rc2[0], rc2[1]);
            out.println("  " + (i+1) + ") ("+rc2[0]+","+rc2[1]+") " + name);
        }
        int rm = readIndex(in, out, "Enter 1.."+chosenCoords.size()+":", 1, chosenCoords.size()) - 1;
        int[] rc = chosenCoords.get(rm);
        removeBuildingAtAndReturnToStack(board, rc[0], rc[1], affected, ctx);
    }

    private int[] pickOneFrom(Principality board, java.util.List<int[]> occupied, src.io.interfaces.IInputService in, src.io.interfaces.IOutputService out, java.util.Set<String> uniq, int ordinal){
        while(true){
            out.println("Pick building #"+ordinal+" (by index):");
            for (int i=0;i<occupied.size();i++){
                int[] rc = occupied.get(i);
                String name = board.getBuildingAt(rc[0], rc[1]);
                out.println("  " + (i+1) + ") ("+rc[0]+","+rc[1]+") " + name);
            }
            try {
                int idx = Integer.parseInt(in.readLine().trim()) - 1;
                if (idx>=0 && idx<occupied.size()){
                    int[] rc = occupied.get(idx);
                    String tag = rc[0]+","+rc[1];
                    if (uniq.add(tag)) return rc;
                    out.println("Already selected; pick a different building.");
                }
            } catch(Exception ignore){ }
            out.println("Please enter a valid index.");
        }
    }

    private void removeBuildingAtAndReturnToStack(Principality board, int row, int col, src.model.Player owner, GameContext ctx){
        var out = ctx.out();
        String name = board.getBuildingAt(row, col);
        if (name == null) { out.println("No building at selected site."); return; }

        // Adjust points if known building types
        if (name.equalsIgnoreCase("Abbey")) {
            owner.addProgressPoints(-1);
        } else if (isCommonHero(name)) {
            // Reverse hero SP/FP based on known table
            var pts = owner.principality().getPoints();
            // Defaults (0,0); use specific mappings
            int sp = 0, fp = 0;
            String n = name.toLowerCase(java.util.Locale.ROOT);
            if (n.equals("austin")) { sp = -1; fp = -2; }
            else if (n.equals("candamir")) { sp = -4; fp = -1; }
            else if (n.equals("harald")) { sp = -2; fp = -1; }
            else if (n.equals("inga")) { sp = -1; fp = -3; }
            else if (n.equals("osmund")) { sp = -2; fp = -2; }
            else if (n.equals("siglind")) { sp = -2; fp = -3; }
            pts.addSP(sp); pts.addFP(fp);
        } else {
            // Handle CP-bearing buildings: Toll Bridge and Trade Ships
            String n = name.toLowerCase(java.util.Locale.ROOT).trim();
            if (n.equals("toll bridge") || n.equals("lumber ship") || n.equals("brick ship")
                || n.equals("grain ship") || n.equals("ore ship") || n.equals("wool ship")
                || n.equals("gold ship") || n.equals("large trade ship")) {
                owner.principality().getPoints().addCP(-1);
            }
        }

        board.clearBuildingAt(row, col);
    out.println("Feud: Removed '"+name+"' at ("+row+","+col+"). Returning to bottom of a matching basic stack.");

        // Place a BasicCard with same name to the bottom of a chosen stack (best effort)
        int st = readIndex(ctx.in(), out, "Choose basic stack [1..4] to return the card:", 1, 4);
        ctx.decks().placeBasicCardToBottom(new BasicCard(name), st);
        // After removal, re-evaluate in case points changed
        src.controller.AdvantageManager.updateAll(ctx);
    }

    private boolean isCommonHero(String name){
        if (name == null) return false;
        String n = name.toLowerCase(java.util.Locale.ROOT).trim();
        return n.equals("austin")||n.equals("candamir")||n.equals("harald")||n.equals("inga")||n.equals("osmund")||n.equals("siglind");
    }

    private int chooseBuildingIndex(src.io.interfaces.IInputService in, src.io.interfaces.IOutputService out, Principality board){
        var sites = board.getOccupiedBuildingSites();
        for (int i=0;i<sites.size();i++){
            int[] rc = sites.get(i);
            String name = board.getBuildingAt(rc[0], rc[1]);
            out.println("  "+(i+1)+") ("+rc[0]+","+rc[1]+") "+name);
        }
        return readIndex(in, out, "Enter 1.."+sites.size()+":", 1, sites.size()) - 1;
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
