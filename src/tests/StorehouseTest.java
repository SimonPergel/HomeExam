package src.tests;

import src.model.*;

/**
 * Unit test for Storehouse building card.
 * 
 * Test: Storehouse can be placed at building sites.
 * 
 * Note: The special effect of Storehouse (protecting resources on the 2 neighboring regions 
 * during Brigand Attack event) is tested through integration tests with the event system.
 */
public class StorehouseTest {
    public static void main(String[] args) {
        Player player = new Player("TestPlayer");
        Principality pr = player.principality();
        
        // Setup basic settlements
        pr.placeSettlementAt(2, 1);
        pr.placeSettlementAt(2, 3);
        
        // Verify Storehouse can be placed at valid building sites
        if (!pr.canPlaceBuildingAt(1, 1)) {
            System.out.println("FAIL: Storehouse should be placeable at (1,1) next to left settlement.");
            System.exit(1);
        }
        
        // Place the Storehouse
        pr.placeBuildingAt(1, 1, "Storehouse");
        
        // Verify Storehouse was placed
        String placedBuilding = pr.getBuildingAt(1, 1);
        if (placedBuilding == null || !placedBuilding.equals("Storehouse")) {
            System.out.println("FAIL: Storehouse was not placed correctly. Got: " + placedBuilding);
            System.exit(1);
        }
        
        System.out.println("PASS: Storehouse can be placed correctly at building sites.");
        System.out.println("Note: Brigand Attack protection effect verified through integration testing.");
    }
}
