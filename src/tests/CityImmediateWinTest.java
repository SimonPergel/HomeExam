package src.tests;

import src.controller.*;
import src.io.MockIO;
import src.model.*;
import src.util.*;

/**
 * Verifies that when a player reaches 7 VP during the action phase (by upgrading to City),
 * the game immediately announces the winner.
 */
public class CityImmediateWinTest {
    public static void main(String[] args) {
        // Script: select the first card from hand (index 0) and provide City coordinates "2 3"
        // which targets the right inner settlement in the intro layout.
        MockIO io = new MockIO().script(
            "1",     // play first hand card (City) — action menu uses 1-based index
            "2 3"    // upgrade at row 2, display col 3 (right inner settlement)
        );

        Randomizer rng = new Randomizer(42);
        GameConfig cfg = new GameConfig();
        DeckManager decks = new DeckManager(rng, cfg);
        RuleValidator rules = new RuleValidator(cfg);
        EventManager eventMgr = new EventManager(new Dice(() -> 2), EffectCatalog.baseGame());
        TurnManager turns = new TurnManager(new Dice(() -> 6), eventMgr, rules);
        GameController game = new GameController(turns, decks, rules, rng, cfg, io, io);

        Player A = new Player("A");
        Player B = new Player("B");

        // Seed starter boards
        SetupService.seedIntroPrincipality(A, 1);
        SetupService.seedIntroPrincipality(B, 2);

        // Boost A close to victory: starter gives 2 VP, add +4 => 6 VP
        A.principality().getPoints().addVP(4);

        // Give A a City card in hand (basic card with name "City"; no cost enforced here)
        A.addToHand(new BasicCard("City"));

        // Run just A's action phase; scripted inputs perform the City upgrade
        game.actionPhase(A, B);

        String log = io.getLog();
        if (log.contains("Winner: A")) {
            System.out.println("PASS: Winner announced immediately upon reaching 7 VP during action phase.");
        } else {
            System.out.println("FAIL: Winner announcement not found. Log was:\n" + log);
            System.exit(1);
        }
    }
}
