package src.controller.actions;

import src.controller.ICardEffect;
import src.controller.GameContext;

public final class BrigittaTheWiseEffect implements ICardEffect, PreRollEffect {

    @Override public void apply(GameContext ctx) {
        // Must be played BEFORE dice are rolled
        if (ctx.turn() != null && ctx.turn().areDiceRolled()) {
            ctx.out().println("You must play Brigitta before rolling the dice this turn.");
            return;
        }
        
        // Mark opponent to have event die control on their next turn
        ctx.opponent().markBrigittaEventDieControl();
        
        ctx.out().println("Brigitta, the Wise Woman played! On the opponent's next turn, you will choose the event die result.");
        ctx.outOpponent().println("Opponent played Brigitta, the Wise Woman! On your next turn, they will choose the event die result.");
    }
}
