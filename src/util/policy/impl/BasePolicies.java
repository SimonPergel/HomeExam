package src.util.policy.impl;
import java.util.Map;
import java.util.List;
import src.util.GameConfig;
import src.util.policy.*;
import src.model.*;
/** Default policies delegating to GameConfig for the base game. */
public class BasePolicies implements ProductionPolicy, VictoryPolicy, TradePolicy, PlacementPolicy {
    private final GameConfig cfg;
    public BasePolicies(GameConfig cfg){ this.cfg = cfg; }
    @Override public Map<String, List<Integer>> regionDice(){ return cfg.regionDice(); }
    @Override public int victoryPointsToWin(){ return cfg.victoryPointsToWin(); }
    @Override public int defaultRatio(){ return 3; }
    @Override public int specializedRatio(){ return 2; }
    @Override public String validatePlacement(Principality princ, BasicCard card){ return null; }
}
