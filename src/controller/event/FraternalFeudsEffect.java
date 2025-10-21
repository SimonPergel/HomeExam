package src.controller.event;

import src.controller.GameContext;
import src.model.BasicCard;
import src.controller.ICardEffect;

/**
 * Fraternal Feuds (1):
 * If you have the strength advantage, your opponent gives you all his/her hand cards.
 * You choose two of them and place them under draw stacks whose cards have matching backs.
 * That is, either both on the bottom of the same stack or one each on the bottom of 2 stacks.
 * Your opponent does NOT know which stack(s). The remaining cards are returned to the opponent.
 * Opponent may not replenish until the end of his/her next turn.
 *
 * NOTE: We implement the hand take/choose/return and bottom-placement. The “no replenish next turn”
 * flag is not present in the current engine, so we log that instruction (TODO hook).
 */
public class FraternalFeudsEffect implements ICardEffect {
    @Override public void apply(GameContext ctx) {
        var in  = ctx.in();
        var out = ctx.out();
        var you = ctx.current();
        var opp = ctx.opponent();

        if (!ctx.rules().hasStrengthAdvantage(you, opp)) {
            out.println("Event: Fraternal Feuds — no strength advantage, no effect.");
            return;
        }

        out.println("Event: Fraternal Feuds — You have strength advantage. Take your opponent's hand.");
        var oppHand = opp.getHandSnapshot(); // snapshot
        if (oppHand.isEmpty()) {
            out.println("Fraternal Feuds: opponent has no cards in hand.");
            return;
        }

        // Copy to your temporary view
        java.util.List<BasicCard> taken = new java.util.ArrayList<>(oppHand);
        // Clear opponent's hand (we must remove exactly; use removeFromHand iteratively)
        for (int i = opp.getHandSize() - 1; i >= 0; i--) opp.removeFromHand(i);

        // Show cards you took
        out.println("You took " + taken.size() + " card(s):");
        for (int i=0;i<taken.size();i++) out.println("  " + (i+1) + ") " + taken.get(i).getName());

        // Choose two indices
        int idx1 = readIndex(in, out, "Pick the FIRST card to bottom-place (1.."+taken.size()+"):", 1, taken.size());
        int idx2 = readIndex(in, out, "Pick the SECOND card to bottom-place (1.."+taken.size()+", can be the same as first only if duplicates):", 1, taken.size());
        BasicCard c1 = taken.remove(idx1-1);
        // adjust second index if necessary
        if (idx2-1 >= taken.size()+1) idx2 = taken.size(); // safety
        BasicCard c2 = taken.remove(Math.max(0, idx2-1));

        // Choose where to bottom-place: stack 1..4 each (matching backs == basic stacks)
        int sA = readIndex(in, out, "Choose BASIC stack (1..4) for first card:", 1, 4);
        int sB = readIndex(in, out, "Choose BASIC stack (1..4) for second card:", 1, 4);

        ctx.decks().placeBasicCardToBottom(c1, sA);
        ctx.decks().placeBasicCardToBottom(c2, sB);

        // Remaining cards go back to opponent
        for (BasicCard c : taken) opp.addToHand(c);

    out.println("Fraternal Feuds: placed 2 card(s) to bottom of stack(s). Opponent gets the rest back.");
    // Enforce: opponent may NOT replenish at the end of their next turn
    opp.markSkipReplenishOnce();
    out.println("Fraternal Feuds: Opponent may NOT replenish until the end of their NEXT turn.");
    }

    private int readIndex(src.io.interfaces.IInputService in, src.io.interfaces.IOutputService out, String prompt, int lo, int hi){
        while(true){
            out.println(prompt);
            try{
                int v = Integer.parseInt(in.readLine().trim());
                if (v>=lo && v<=hi) return v;
            }catch(Exception ignore){}
            out.println("Please enter a number between " + lo + " and " + hi + ".");
        }
    }
}