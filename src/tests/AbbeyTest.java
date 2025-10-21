package src.tests;

import src.model.*;

/**
 * Unit test for Abbey building card.
 * 
 * Test: Abbey gives 1 Progress Point (PP).
 */
public class AbbeyTest {
    public static void main(String[] args) {
        // Create a principality and add points
        Principality principality = new Principality();
        
        // Verify initial PP is 0
        if (principality.getProgressPoints() != 0) {
            System.out.println("FAIL: Initial PP should be 0, got " + principality.getProgressPoints());
            System.exit(1);
        }
        
        // Abbey gives 1 PP
        principality.getPoints().addPP(1);
        
        // Verify PP is now 1
        if (principality.getProgressPoints() != 1) {
            System.out.println("FAIL: After adding Abbey, PP should be 1, got " + principality.getProgressPoints());
            System.exit(1);
        }
        
        System.out.println("PASS: Abbey correctly provides 1 Progress Point (PP).");
    }
}
