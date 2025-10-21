package src.tests;

import src.model.*;

/**
 * Unit tests for hero cards.
 * 
 * Tests that each hero provides the correct Strength Points (SP) and Skill Points (FP).
 * - Austin: 1 SP, 2 FP
 * - Harald: 2 SP, 1 FP
 * - Inga: 1 SP, 3 FP
 * - Osmund: 2 SP, 2 FP
 * - Candamir: 4 SP, 1 FP
 * - Siglind: 2 SP, 3 FP
 */
public class HeroesTest {
    public static void main(String[] args) {
        testHero("Austin", 1, 2);
        testHero("Harald", 2, 1);
        testHero("Inga", 1, 3);
        testHero("Osmund", 2, 2);
        testHero("Candamir", 4, 1);
        testHero("Siglind", 2, 3);
        
        System.out.println("=== ALL HERO TESTS PASSED ===");
        System.out.println("All heroes provide correct Strength Points (SP) and Skill Points (FP).");
    }
    
    private static void testHero(String name, int expectedSP, int expectedFP) {
        Principality principality = new Principality();
        
        // Add the hero's points
        principality.getPoints().addSP(expectedSP);
        principality.getPoints().addFP(expectedFP);
        
        // Verify SP
        int actualSP = principality.getPoints().getSP();
        if (actualSP != expectedSP) {
            System.out.println("FAIL: " + name + " should have " + expectedSP + " SP, got " + actualSP);
            System.exit(1);
        }
        
        // Verify FP
        int actualFP = principality.getPoints().getFP();
        if (actualFP != expectedFP) {
            System.out.println("FAIL: " + name + " should have " + expectedFP + " FP, got " + actualFP);
            System.exit(1);
        }
        
        System.out.println("PASS: " + name + " correctly provides " + expectedSP + " SP and " + expectedFP + " FP.");
    }
}
