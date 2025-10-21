package src.tests;

import src.controller.*;
import src.io.MockIO;
import src.model.*;
import src.util.*;

public class GoldsmithHappyTest {
    public static void main(String[] args) {
        // Script picks: wood, ore
        MockIO io = new MockIO().script("wood", "ore");

        DeckManager decks = new DeckManager(new Randomizer(42), new GameConfig());
        RuleValidator rules = new RuleValidator(new GameConfig());
        TurnState turn = new TurnState();
        Player A = new Player("A");
        Player B = new Player("B");

        // A has exactly 3 GOLD
        A.setResource(Resource.GOLD, 3);

    GameContext ctx = new GameContext(A, B, io, io, io, io, decks, rules, turn);

        new src.controller.actions.GoldsmithEffect().apply(ctx);
        // this checks the resource bank.
        boolean ok = A.getResource(Resource.GOLD) == 0
                  && A.getResource(Resource.WOOD) == 1
                  && A.getResource(Resource.ORE)  == 1;

        if (ok) {
            System.out.println("PASS: Goldsmith converts 3 GOLD -> +1 WOOD, +1 ORE.");
        } else {
            System.out.println("FAIL: Resources were GOLD=" + A.getResource(Resource.GOLD)
                + " WOOD=" + A.getResource(Resource.WOOD)
                + " ORE=" + A.getResource(Resource.ORE));
            System.exit(1);
        }
    }
}