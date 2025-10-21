package src.tests;

import src.controller.*;
import src.io.MockIO;
import src.model.*;
import src.util.*;

public class GoldsmithInsufficientTest {
    public static void main(String[] args) {
        MockIO io = new MockIO(); // no inputs needed

        DeckManager decks = new DeckManager(new Randomizer(42), new GameConfig());
        RuleValidator rules = new RuleValidator(new GameConfig());
        TurnState turn = new TurnState();
        Player A = new Player("A");
        Player B = new Player("B");

        // Only 2 GOLD -> not enough
        A.setResource(Resource.GOLD, 2);

    GameContext ctx = new GameContext(A, B, io, io, io, io, decks, rules, turn);
        new src.controller.actions.GoldsmithEffect().apply(ctx);

        boolean ok = io.getLog().contains("Goldsmith: you need at least 3 GOLD.")
                  && A.getResource(Resource.GOLD) == 2;

        if (ok) {
            System.out.println("PASS: Goldsmith blocked with <3 GOLD.");
        } else {
            System.out.println("FAIL: Expected block message and unchanged gold. Log:\n" + io.getLog());
            System.exit(1);
        }
    }
}