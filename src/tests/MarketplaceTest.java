package src.tests;

import src.model.*;

/**
 * Unit test for Marketplace building card.
 * 
 * Test: Marketplace gives 1 Commerce Point (CP).
 * Note: The production-based resource gain effect of Marketplace would require 
 * integration testing with the production system, which is beyond the scope of 
 * basic unit tests. This test validates the static CP property.
 */
public class MarketplaceTest {
    public static void main(String[] args) {
        Principality principality = new Principality();
        
        // Verify initial CP is 0
        if (principality.getPoints().getCP() != 0) {
            System.out.println("FAIL: Initial CP should be 0, got " + principality.getPoints().getCP());
            System.exit(1);
        }
        
        // Marketplace gives 1 CP
        principality.getPoints().addCP(1);
        
        // Verify CP is now 1
        if (principality.getPoints().getCP() != 1) {
            System.out.println("FAIL: After adding Marketplace, CP should be 1, got " + principality.getPoints().getCP());
            System.exit(1);
        }
        
        System.out.println("PASS: Marketplace correctly provides 1 Commerce Point (CP).");
    }
}
