package src.tests;

import src.controller.DeckManager;
import src.io.MockIO;
import src.model.BasicCard;
import src.model.Player;
import src.model.Principality;
import src.model.RegionTile;
import src.model.Resource;
import src.util.GameConfig;
import src.util.Randomizer;

/**
 * Verifies that when Parish Hall is placed, Exchange "Search" costs 1 resource (not 2),
 * and exactly one stored resource is deducted from the player's storage-only economy.
 */
public class ParishHallExchangeDiscountTest {
    public static void main(String[] args) {
        // Scripted responses in order:
        // 1) Exchange? -> Y
        // 2) Card name to put under stack -> X1 (we'll add this to hand)
        // 3) Choose stack -> 1
        // 4) Random (R) or Search (S, costs 1 any)? -> S
        // 5) Discard resource #1 -> wood
        // 6) Type exact name to take -> Basic1 (top of stack 1 in placeholder bootstrapping)
        MockIO io = new MockIO().script(
                "Y", "X1", "1", "S", "wood", "Basic1"
        );

        DeckManager decks = new DeckManager(new Randomizer(123), new GameConfig());
        Player A = new Player("A");

        // Ensure we meet the hand size condition (>= 3 + PP)
        A.addToHand(new BasicCard("X1"));
        A.addToHand(new BasicCard("X2"));
        A.addToHand(new BasicCard("X3"));

        // Prepare storage-only resources: add a WOOD tile with 1 stored
        RegionTile woodTile = new RegionTile(Resource.WOOD, 6);
        woodTile.setStored(1);
        A.principality().addRegion(woodTile);

        // Place a left settlement so the building site is valid, then place Parish Hall at (row=1,col=1)
        Principality P = A.principality();
        P.placeSettlementAt(2, 1); // left settlement
        if (!P.canPlaceBuildingAt(1, 1)) {
            System.out.println("FAIL: Expected building site (1,1) to be available.");
            System.exit(1);
        }
        P.placeBuildingAt(1, 1, "Parish Hall");

        // Sanity: discount flag
        if (!P.hasParishHall()) {
            System.out.println("FAIL: Parish Hall not detected after placement.");
            System.exit(1);
        }

        // Capture pre-exchange stored total
        int preStored = totalStored(A);

        // Run the optional exchange flow (uses our scripted IO)
        decks.optionalExchange(A, io, io);

        // Check that exactly 1 stored resource was deducted
        int postStored = totalStored(A);
        boolean paidOne = (preStored - postStored) == 1;

        // Check the prompt text contained "costs 1 any"
        String log = io.getLog();
        boolean showedDiscount = log.contains("costs 1 any");

        if (paidOne && showedDiscount) {
            System.out.println("PASS: Parish Hall reduced Search cost to 1 and exactly one resource was paid.");
        } else {
            System.out.println("FAIL: paidOne=" + paidOne + ", showedDiscount=" + showedDiscount +
                    "; preStored=" + preStored + ", postStored=" + postStored +
                    "\nLOG:\n" + log);
            System.exit(1);
        }
    }

    private static int totalStored(Player p){
        int s = 0;
        for (var t : p.principality().regions()) s += Math.max(0, t.getStored());
        return s;
    }
}
