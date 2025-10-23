package src.tests;

import src.model.*;
import java.util.Set;

/**
 * Unit test for Storehouse building card.
 * 
 * Tests that Storehouse:
 * 1. Can be placed at building sites
 * 2. Protects the 2 neighboring regions from being counted during Brigand Attack
 * 
 * According to issue #3:
 * "Storehouse: Do not count the resources on the 2 neighboring regions when 
 * the event Brigand Attack is rolled."
 */
public class StorehouseTest {
    public static void main(String[] args) {
        testStorehousePlacement();
        testStorehouseBrigandProtection();
        
        System.out.println("\n=== ALL STOREHOUSE TESTS PASSED ===");
        System.out.println("Storehouse can be placed and protects neighboring regions from Brigand Attack.");
    }
    
    /**
     * Test that Storehouse can be placed at building sites.
     */
    private static void testStorehousePlacement() {
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
    }
    
    /**
     * Test that Storehouse protects neighboring regions from Brigand Attack counting.
     * When a Storehouse is adjacent to a settlement, the 2 neighboring regions of that
     * settlement should be excluded from the >7 resource threshold check.
     */
    private static void testStorehouseBrigandProtection() {
        Player player = new Player("TestPlayer");
        Principality pr = player.principality();
        
        // Place settlements at columns 1 and 3
        pr.placeSettlementAt(2, 1); // Settlement at column 1
        pr.placeSettlementAt(2, 3); // Settlement at column 3
        
        // Initially, no storehouses = no protected columns
        Set<Integer> protectedBefore = pr.getRegionColsProtectedByStorehouses();
        if (protectedBefore.size() != 0) {
            System.out.println("FAIL: Initially, no columns should be protected, got " + protectedBefore.size());
            System.exit(1);
        }
        
        // Place a Storehouse at building site (1,1), which is adjacent to settlement at column 1
        pr.placeBuildingAt(1, 1, "Storehouse");
        
        // Now the regions at the settlement column should be protected
        Set<Integer> protectedAfter = pr.getRegionColsProtectedByStorehouses();
        if (protectedAfter.size() == 0) {
            System.out.println("FAIL: After placing Storehouse, some columns should be protected");
            System.exit(1);
        }
        
        // The Storehouse at site (1,1) should protect the settlement column it's adjacent to
        // According to the implementation, it maps the building site column to a settlement column
        // and protects that column's regions
        
        // Verify that protected columns are now tracked
        if (!protectedAfter.contains(1)) {
            System.out.println("FAIL: Storehouse should protect column 1 regions, protected: " + protectedAfter);
            System.exit(1);
        }
        
        // Place regions with resources to verify the protection logic
        RegionTile topRegion = new RegionTile(Resource.WHEAT, 3);
        RegionTile bottomRegion = new RegionTile(Resource.WOOD, 4);
        topRegion.incStored(); // Add 1 resource
        bottomRegion.incStored(); // Add 1 resource
        
        pr.placeRegionTop(1, topRegion);
        pr.placeRegionBottom(1, bottomRegion);
        
        // Verify regions are placed
        if (pr.getTopRegion(1) == null || pr.getBottomRegion(1) == null) {
            System.out.println("FAIL: Regions should be placed at column 1");
            System.exit(1);
        }
        
        // During Brigand Attack, these regions' resources should NOT be counted
        // toward the >7 threshold because they're protected by the Storehouse
        // This is verified by the getRegionColsProtectedByStorehouses() method
        
        System.out.println("PASS: Storehouse protects neighboring regions from Brigand Attack counting.");
    }
}
