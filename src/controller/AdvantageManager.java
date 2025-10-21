package src.controller;

import src.model.Player;

/** Centralized advantage recalculation (trade and strength) and VP awarding/removal.
 * Rules implemented:
 * - Trade Advantage: A player has trade advantage iff CP >= 3 and strictly greater than opponent's CP.
 *   While true, that player holds the Trade Advantage flag and is awarded +1 VP (once). If the condition
 *   becomes false (falls below 3 or no longer strictly greater), remove the flag and remove that +1 VP (if awarded).
 * - Strength Advantage: A player has strength advantage iff FP >= 3 and strictly greater than opponent's FP.
 *   While true, that player is awarded +1 VP (once). If condition fails later, remove that VP.
 *
 * The board printer reflects VP from Principality.Points, so updating VP here automatically updates UI.
 */
public final class AdvantageManager {
    private AdvantageManager() {}

    public static void updateAll(GameContext ctx) {
        Player a = ctx.current();
        Player b = ctx.opponent();
        updateTrade(ctx, a, b);
        updateTrade(ctx, b, a);
        updateStrength(ctx, a, b);
        updateStrength(ctx, b, a);
    }

    private static void updateTrade(GameContext ctx, Player p, Player q) {
        int cpP = p.principality().getPoints().getCP();
        int cpQ = q.principality().getPoints().getCP();
        boolean has = cpP >= 3 && cpP > cpQ;
        boolean flag = p.hasTradeAdvantage();
        if (has && !flag) {
            p.setTradeAdvantage(true);
            if (!p.isTradeAdvVPAwarded()) {
                p.principality().getPoints().addVP(1);
                p.setTradeAdvVPAwarded(true);
                announce(ctx, p, "+1 VP for Trade Advantage (CP="+cpP+")");
            }
        } else if (!has && (flag || p.isTradeAdvVPAwarded())) {
            p.setTradeAdvantage(false);
            if (p.isTradeAdvVPAwarded()) {
                p.principality().getPoints().addVP(-1);
                p.setTradeAdvVPAwarded(false);
                announce(ctx, p, "Trade Advantage lost — -1 VP");
            }
        }
    }

    private static void updateStrength(GameContext ctx, Player p, Player q) {
        int fpP = p.principality().getPoints().getFP();
        int fpQ = q.principality().getPoints().getFP();
        boolean has = fpP >= 3 && fpP > fpQ;
        boolean awarded = p.isStrengthAdvVPAwarded();
        if (has && !awarded) {
            p.principality().getPoints().addVP(1);
            p.setStrengthAdvVPAwarded(true);
            announce(ctx, p, "+1 VP for Strength Advantage (FP="+fpP+")");
        } else if (!has && awarded) {
            p.principality().getPoints().addVP(-1);
            p.setStrengthAdvVPAwarded(false);
            announce(ctx, p, "Strength Advantage lost — -1 VP");
        }
    }

    private static void announce(GameContext ctx, Player p, String msg) {
        String line = p.getName() + ": " + msg;
        ctx.out().println(line);
        ctx.outOpponent().println(line);
    }
}
