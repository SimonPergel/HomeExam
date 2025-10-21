package src.controller;

import src.model.Player;
import src.util.Dice;
import src.controller.phases.PreRollPhase;

public class TurnManager {
    private final Dice productionDie;
    private final EventManager eventMgr;
    private final RuleValidator rules;
    private final PreRollPhase preRoll = new PreRollPhase();

    public TurnManager(Dice productionDie, EventManager eventMgr, RuleValidator rules){
        this.productionDie = productionDie;
        this.eventMgr = eventMgr;
        this.rules = rules;
    }

    public void playTurn(GameController game, Player current, Player opponent){
        // Per-turn state
        TurnState ts = new TurnState();
        game.setCurrentTurnState(ts);

        // Pre-roll phase (e.g., Brigitta)
        preRoll.run(game, current, opponent);

    // Roll event ONCE; production uses override if Brigitta was played
    int eventFace = eventMgr.rollFace();
    int prodRoll  = ts.getProductionOverride().orElse(productionDie.roll(6));

    // Broadcast die faces to both terminals in legacy format
    game.broadcastEventDie(eventFace, current, opponent);

        GameContext ctx = game.makeContext(current, opponent);

        // Resolve in correct order: Brigand (1) before production; else production first
        if (eventFace == 1) {
            eventMgr.resolveFace(ctx, game.getDecks(), eventFace);
            game.produce(prodRoll, current, opponent);
        } else {
            game.produce(prodRoll, current, opponent);
            eventMgr.resolveFace(ctx, game.getDecks(), eventFace);
        }

        ts.markDiceRolled();

        // After Part 1 (dice resolved), print recap on both terminals
        game.printTurnRecap(current, opponent);

        // Action phase & end-of-turn flow
        game.actionPhase(current, opponent);
        game.replenish(current);
        game.exchange(current);
        if (rules.hasWon(current)) {
            game.declareWinner(current);
        }
    }
}
