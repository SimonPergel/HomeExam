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
        int face = askInt(ctx, "Choose the production die result (1-6): ", 1, 6);
        if (ctx.turn() != null) {
            ctx.turn().setProductionOverride(face);
        }
        ctx.out().println("Brigitta sets this turn’s production die to " + face + ".");
    }

    private int askInt(GameContext ctx, String prompt, int min, int max) {
        while (true) {
            ctx.out().print(prompt);
            String s = ctx.in().readLine();
            if (s == null) return min;
            try {
                int v = Integer.parseInt(s.trim());
                if (v >= min && v <= max) return v;
            } catch (NumberFormatException ignored) {}
            ctx.out().println("Please enter a number from " + min + " to " + max + ".");
        }
    }
}
