package src.tests;

import src.controller.*;
import src.model.*;
import src.io.MockIO;
import src.util.*;

public class CityUpgradeTest {
    public static void main(String[] args) {
    MockIO io = new MockIO();
    // Simulate entering coordinates "2 1" for the upgrade prompt
    io.script("2 1");

        Dice prodDie = new Dice(() -> 1);
        EventManager eventMgr = new EventManager(new Dice(() -> 2), EffectCatalog.baseGame());
        RuleValidator rules = new RuleValidator(new GameConfig());
        TurnManager turns = new TurnManager(prodDie, eventMgr, rules);
        DeckManager decks = new DeckManager(new Randomizer(42), new GameConfig());
        GameController game = new GameController(turns, decks, rules, new Randomizer(42), new GameConfig(), io, io);

        Player A = new Player("A");
        SetupService.seedIntroPrincipality(A, 1);
        Principality pr = A.principality();

        int vpBefore = pr.getPoints().getVP();
        var ctx = game.makeContext(A, new Player("B"));

        // Perform city upgrade via placement handler
        boolean ok = new src.controller.placement.CityPlacementHandler().place(new BasicCard("City"), ctx);
        if (!ok) { System.out.println("FAIL: City upgrade handler returned false."); System.exit(1); }

        // Check VP increased by 1
        int vpAfter = pr.getPoints().getVP();
        if (vpAfter != vpBefore + 1) { System.out.println("FAIL: VP did not increase by 1 on upgrade."); System.exit(1); }

        // Extra building sites should now be available at display col 2 for row 1 and 3
        // The printer shifts left by 0 initially; so display col 2 maps to logical col 2 when left city
        if (!pr.canPlaceBuildingAtDisplay(1, 2) || !pr.canPlaceBuildingAtDisplay(3, 2)) {
            System.out.println("FAIL: Extra building sites not available after city upgrade.");
            System.exit(1);
        }
        System.out.println("PASS: City upgrade unlocks extra sites and increases VP.");
    }
}
