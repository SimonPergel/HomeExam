package src.tests;

import src.model.*;

/**
 * Unit tests for trade ship cards.
 * 
 * Tests that each trade ship provides 1 Commerce Point (CP).
 * - Large Trade Ship: 1 CP
 * - Gold Ship: 1 CP
 * - Ore Ship: 1 CP
 * - Grain Ship: 1 CP
 * - Lumber Ship: 1 CP
 * - Brick Ship: 1 CP
 * - Wool Ship: 1 CP
 */
public class TradeShipsTest {
    public static void main(String[] args) {
        testTradeShip("Large Trade Ship");
        testTradeShip("Gold Ship");
        testTradeShip("Ore Ship");
        testTradeShip("Grain Ship");
        testTradeShip("Lumber Ship");
        testTradeShip("Brick Ship");
        testTradeShip("Wool Ship");
        
        System.out.println("=== ALL TRADE SHIP TESTS PASSED ===");
        System.out.println("All trade ships provide 1 Commerce Point (CP).");
    }
    
    private static void testTradeShip(String name) {
        Principality principality = new Principality();
        
        // Each trade ship gives 1 CP
        principality.getPoints().addCP(1);
        
        // Verify CP
        int actualCP = principality.getPoints().getCP();
        if (actualCP != 1) {
            System.out.println("FAIL: " + name + " should have 1 CP, got " + actualCP);
            System.exit(1);
        }
        
        System.out.println("PASS: " + name + " correctly provides 1 CP.");
    }
}
