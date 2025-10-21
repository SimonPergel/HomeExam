package src.tests;

import src.model.*;

/**
 * Unit tests for production booster buildings.
 * 
 * Tests that production booster buildings can be placed correctly:
 * - Iron Foundry: Doubles ore production of neighboring mountains
 * - Grain Mill: Doubles grain production of neighboring fields
 * - Lumber Camp: Doubles lumber production of neighboring forests
 * - Brick Factory: Doubles brick production of neighboring hills
 * - Weaver's Shop: Doubles wool production of neighboring pastures
 * 
 * Note: The actual production doubling effect is tested through integration tests
 * with the DeckManager, as the production logic is encapsulated in that class.
 */
public class ProductionBoostersTest {
    public static void main(String[] args) {
        testBuildingPlacement("Iron Foundry");
        testBuildingPlacement("Grain Mill");
        testBuildingPlacement("Lumber Camp");
        testBuildingPlacement("Brick Factory");
        testBuildingPlacement("Weaver's Shop");
        
        System.out.println("\n=== ALL PRODUCTION BOOSTER TESTS PASSED ===");
        System.out.println("All production booster buildings can be placed correctly:");
        System.out.println("  - Iron Foundry (doubles ore from mountains)");
        System.out.println("  - Grain Mill (doubles grain from fields)");
        System.out.println("  - Lumber Camp (doubles lumber from forests)");
        System.out.println("  - Brick Factory (doubles brick from hills)");
        System.out.println("  - Weaver's Shop (doubles wool from pastures)");
        System.out.println("\nNote: Production doubling effects are verified through integration testing.");
    }
    
    private static void testBuildingPlacement(String buildingName) {
        Player player = new Player("TestPlayer");
        Principality pr = player.principality();
        
        // Setup basic settlements
        pr.placeSettlementAt(2, 1);
        pr.placeSettlementAt(2, 3);
        
        // Verify building can be placed at valid building sites
        if (!pr.canPlaceBuildingAt(1, 1)) {
            System.out.println("FAIL: " + buildingName + " should be placeable at (1,1) next to left settlement.");
            System.exit(1);
        }
        
        // Place the building
        pr.placeBuildingAt(1, 1, buildingName);
        
        // Verify building was placed
        String placedBuilding = pr.getBuildingAt(1, 1);
        if (placedBuilding == null || !placedBuilding.equals(buildingName)) {
            System.out.println("FAIL: " + buildingName + " was not placed correctly. Got: " + placedBuilding);
            System.exit(1);
        }
        
        // Verify building cannot be placed at the same location again
        if (pr.canPlaceBuildingAt(1, 1)) {
            System.out.println("FAIL: " + buildingName + " location should be occupied.");
            System.exit(1);
        }
        
        System.out.println("PASS: " + buildingName + " can be placed correctly at building sites.");
    }
}
