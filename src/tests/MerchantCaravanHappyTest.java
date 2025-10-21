package src.tests;

import src.controller.*;
import src.io.MockIO;
import src.model.*;
import src.util.*;

public class MerchantCaravanHappyTest {
    public static void main(String[] args) {
        // Script:
        // "wood brick" -> discard
        // "ore wheat"  -> gain
        MockIO io = new MockIO().script("wood brick", "ore wheat");

        DeckManager decks = new DeckManager(new Randomizer(42), new GameConfig());
        RuleValidator rules = new RuleValidator(new GameConfig());
        TurnState turn = new TurnState();
        Player A = new Player("A");
        Player B = new Player("B");

        // Give resources to discard
        A.setResource(Resource.WOOD, 1);
        A.setResource(Resource.BRICK, 1);

    GameContext ctx = new GameContext(A, B, io, io, io, io, decks, rules, turn);
        new src.controller.actions.MerchantCaravanEffect().apply(ctx);

        boolean ok = A.getResource(Resource.WOOD)  == 0
                  && A.getResource(Resource.BRICK) == 0
                  && A.getResource(Resource.ORE)   == 1
                  && A.getResource(Resource.WHEAT) == 1;

        if (ok) {
            System.out.println("PASS: Merchant Caravan swapped (WOOD,BRICK) -> (ORE,WHEAT).");
        } else {
            System.out.println("FAIL: Counts were WOOD=" + A.getResource(Resource.WOOD)
                + " BRICK=" + A.getResource(Resource.BRICK)
                + " ORE=" + A.getResource(Resource.ORE)
                + " WHEAT=" + A.getResource(Resource.WHEAT));
            System.exit(1);
        }
    }
}