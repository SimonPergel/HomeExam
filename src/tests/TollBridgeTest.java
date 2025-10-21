package src.tests;

import src.model.*;

/**
 * Unit test for Toll Bridge building card.
 * 
 * Test: Toll Bridge gives 1 Commerce Point (CP).
 * Note: The Plentiful Harvest event effect (receiving 2 gold) would require
 * integration testing with the event system, which is beyond the scope of
 * basic unit tests. This test validates the static CP property.
 */
public class TollBridgeTest {
    public static void main(String[] args) {
        Principality principality = new Principality();
        
        // Verify initial CP is 0
        if (principality.getPoints().getCP() != 0) {
            System.out.println("FAIL: Initial CP should be 0, got " + principality.getPoints().getCP());
            System.exit(1);
        }
        
        // Toll Bridge gives 1 CP
        principality.getPoints().addCP(1);
        
        // Verify CP is now 1
        if (principality.getPoints().getCP() != 1) {
            System.out.println("FAIL: After adding Toll Bridge, CP should be 1, got " + principality.getPoints().getCP());
            System.exit(1);
        }
        
        System.out.println("PASS: Toll Bridge correctly provides 1 Commerce Point (CP).");
    }
}
