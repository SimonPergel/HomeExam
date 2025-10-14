package src.model;

public class VictoryPointTracker {
    public static int computeScore(Player p, Player opp) {
        int score = p.victoryPoints;
        if ((p.commercePoints - (opp == null ? 0 : opp.commercePoints)) >= 3) score += 1;
        if ((p.strengthPoints - (opp == null ? 0 : opp.strengthPoints)) >= 3) score += 1;
        return score;
    }
}
