package src.tests;

import src.controller.*;
import src.io.MockIO;
import src.model.*;
import src.util.*;

public class MerchantCaravanDoubleDiscardTest {
    public static void main(String[] args) {
        // "wool wool" discard, then "gold gold" gain
        MockIO io = new MockIO().script("wool wool", "gold gold");

        DeckManager decks = new DeckManager(new Randomizer(42), new GameConfig());
        RuleValidator rules = new RuleValidator(new GameConfig());
        TurnState turn = new TurnState();
        Player A = new Player("A");
        Player B = new Player("B");

        A.setResource(Resource.WOOL, 2);

    GameContext ctx = new GameContext(A, B, io, io, io, io, decks, rules, turn);
        new src.controller.actions.MerchantCaravanEffect().apply(ctx);

        boolean ok = A.getResource(Resource.WOOL) == 0
                  && A.getResource(Resource.GOLD) == 2;

        if (ok) {
            System.out.println("PASS: Merchant Caravan (wool wool) -> (gold gold).");
        } else {
            System.out.println("FAIL: WOOL=" + A.getResource(Resource.WOOL)
                + " GOLD=" + A.getResource(Resource.GOLD));
            System.exit(1);
        }
    }
}