package src.tests;

import src.controller.*;
import src.io.MockIO;
import src.model.*;
import src.util.*;

/**
 * Verifies Large Trade Ship parity on far rows (row 4 behaves like row 3 for adjacency).
 * Scenario:
 * - Seed intro board for Player A (has bottom row col4 = ORE).
 * - Upgrade right settlement to city to unlock far-bottom right building site (row 4, col 3).
 * - Place Large Trade Ship at (4,3).
 * - Ensure adjacent bottom region at (3,4) has >=2 ORE stored.
 * - Run action phase command: "LTS R ore wood".
 * - Expect: ORE at (3,4) decreases by 2; any WOOD region gains +1 (top col0 Forest has capacity).
 */
public class LTSParityTest {
    public static void main(String[] args) {
        // IO scripts the LTS command then ends the action phase
        MockIO io = new MockIO().script(
            // Action Phase loop: enter LTS command, then END
            "LTS R ore wood",
            "END"
        );

        // Deterministic dice not essential for this test
        Dice prodDie = new Dice(() -> 1);
        EventManager eventMgr = new EventManager(new Dice(() -> 2), EffectCatalog.baseGame());
        RuleValidator rules = new RuleValidator(new GameConfig());
        TurnManager turns = new TurnManager(prodDie, eventMgr, rules);
        DeckManager decks = new DeckManager(new Randomizer(42), new GameConfig());
        GameController game = new GameController(turns, decks, rules, new Randomizer(42), new GameConfig(), io, io);

        Player A = new Player("A");
        Player B = new Player("B");

    // Seed intro principality (gives bottom row col4 = ORE with stored 1)
        SetupService.seedIntroPrincipality(A, 1);

    // Ensure action phase shows full menu (non-empty hand) so LTS command is accepted
    A.addToHand(new BasicCard("Placeholder"));

        Principality pr = A.principality();
        // Upgrade right to a city (required to place at far row 4, col 3)
        if (!pr.hasSettlementRight()) {
            System.out.println("FAIL: Expected right settlement present after seeding.");
            System.exit(1);
        }
        if (!pr.canPlaceCityAt(2, 3)) {
            System.out.println("FAIL: Cannot upgrade right settlement to city.");
            System.exit(1);
        }
        pr.upgradeCityAt(2, 3);
        if (!pr.hasCityRight()) {
            System.out.println("FAIL: CityRight flag not set after upgrade.");
            System.exit(1);
        }

        // Place Large Trade Ship at far-bottom right site (row 4, col 3)
        if (!pr.canPlaceBuildingAt(4, 3)) {
            System.out.println("FAIL: Cannot place Large Trade Ship at (4,3) even after right city.");
            System.exit(1);
        }
        pr.placeBuildingAt(4, 3, "Large Trade Ship");
        if (!pr.hasBuildingNamed("Large Trade Ship")) {
            System.out.println("FAIL: Large Trade Ship not registered on board.");
            System.exit(1);
        }

        // Ensure adjacent region (effective row 3, col 4) has at least 2 ORE stored
        RegionTile oreTile = pr.getRegionAt(3, 4);
        if (oreTile == null || oreTile.getResource() != Resource.ORE) {
            System.out.println("FAIL: Expected ORE region at (3,4).");
            System.exit(1);
        }
        oreTile.setStored(2); // make it tradable

        // Capture pre counts
        int oreBefore = oreTile.getStored();
        RegionTile woodTile = pr.getRegionAt(1, 0); // Forest at top-left by seed
        if (woodTile == null || woodTile.getResource() != Resource.WOOD) {
            System.out.println("FAIL: Expected WOOD region at (1,0).");
            System.exit(1);
        }
        int woodBefore = woodTile.getStored();

        // Proceed to action phase where LTS command will be processed

        // Run just the action phase for A (command is scripted in IO)
        game.actionPhase(A, B);

        // Validate results: -2 ORE at (3,4), +1 WOOD somewhere (specifically top-left forest)
        int oreAfter = oreTile.getStored();
        int woodAfter = woodTile.getStored();

        boolean ok = (oreBefore - oreAfter == 2) && (woodAfter - woodBefore == 1);
        if (ok) {
            System.out.println("PASS: LTS parity on far-bottom row works (adjacent row 3 used).");
        } else {
            System.out.println("FAIL: Unexpected storage deltas. ORE was " + oreBefore + ", now " + oreAfter
                + "; WOOD was " + woodBefore + ", now " + woodAfter + ".");
            // Dump interaction log to help diagnose why LTS command may not have applied
            System.out.println("--- IO LOG ---\n" + io.getLog());
            System.exit(1);
        }
    }
}
