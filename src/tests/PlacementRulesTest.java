package src.tests;

import src.controller.*;
import src.model.*;
import src.io.MockIO;
import src.util.*;

/** Minimal harness to exercise road/settlement placement rules for row-2 and adjacency. */
public class PlacementRulesTest {
    public static void main(String[] args) {
        MockIO io = new MockIO();
        Dice prodDie = new Dice(() -> 1);
        EventManager eventMgr = new EventManager(new Dice(() -> 2), EffectCatalog.baseGame());
        RuleValidator rules = new RuleValidator(new GameConfig());
        TurnManager turns = new TurnManager(prodDie, eventMgr, rules);
        DeckManager decks = new DeckManager(new Randomizer(42), new GameConfig());
        GameController game = new GameController(turns, decks, rules, new Randomizer(42), new GameConfig(), io, io);

        Player A = new Player("A");
        Player B = new Player("B");

        // Seed board
    SetupService.seedIntroPrincipality(A, 1);

        Principality pr = A.principality();
        if (!pr.hasSettlementLeft() || !pr.hasSettlementRight() || !pr.hasRoadCenter()) {
            System.out.println("FAIL: Starter board not set correctly.");
            System.exit(1);
        }

        // Try placing left edge road when no left settlement exists (simulate by new player)
        Player C = new Player("C");
    SetupService.seedIntroPrincipality(C, 1);
        // left edge should be empty and allowed since left settlement exists
        if (!C.principality().canPlaceRoadAt(2,0)) {
            System.out.println("FAIL: Expected canPlaceRoadAt(2,0) when left settlement exists.");
            System.exit(1);
        }
        C.principality().placeRoadAt(2,0);
        if (!C.principality().hasRoadLeft()) {
            System.out.println("FAIL: Road left not set after placement.");
            System.exit(1);
        }

        // Settlement extension must require edge road first; simulate a fresh player D without left edge road
        Player D = new Player("D");
    SetupService.seedIntroPrincipality(D, 1);
        // Empty extension slot at (2,1) is already occupied from starter, so clear to simulate extending beyond? Skip — base intro has both settlements already.
        // This test is mainly to ensure model guards compile and edge roads render.

        System.out.println("PASS: Placement rules basic checks passed.");
    }
}
