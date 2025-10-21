package src.tests;

import src.controller.*;
import src.model.*;
import src.io.MockIO;
import src.util.*;

public class BrigittaPreRollTest {
    public static void main(String[] args) {
        // Scripted inputs:
        // 1 -> pick Brigitta in pre-roll list
        // 3 -> choose production die face
        // skip -> skip action phase
        // 1 -> choose stack on replenish
        // N -> no exchange
        MockIO io = new MockIO().script("1","3","skip","1","N");

        // Deterministic dice: production die would roll 6 (but Brigitta overrides to 3); event die rolls 2 (not Brigand)
        Dice productionDie = new Dice(() -> 6);
        EventManager eventMgr = new EventManager(new Dice(() -> 2), EffectCatalog.baseGame());
        RuleValidator rules = new RuleValidator(new GameConfig());
        TurnManager turns = new TurnManager(productionDie, eventMgr, rules);

        // Minimal decks (placeholder mode is fine)
        DeckManager decks = new DeckManager(new Randomizer(42), new GameConfig());

        // Controller
        GameController game = new GameController(turns, decks, rules, new Randomizer(42), new GameConfig(), io, io);

        // Players
        Player A = new Player("A");
        Player B = new Player("B");

        // Give A the Brigitta card in hand
        ICardEffect brigEff = EffectCatalog.baseGame().forKey("brigitta");
        BasicCard brigitta = new BasicCard("Brigitta, the Wise Woman", brigEff);
        A.addToHand(brigitta);

        // Run a single turn
        turns.playTurn(game, A, B);

        String log = io.getLog();
        boolean ok = log.contains("Brigitta sets this turn’s production die to 3.")
                  && log.contains("Production roll: 3");

        if (ok) {
            System.out.println("PASS: Brigitta pre-roll override applied (production=3).");
        } else {
            System.out.println("FAIL. Log was:\n" + log);
            System.exit(1);
        }
    }
}