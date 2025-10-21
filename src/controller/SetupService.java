package src.controller;

import src.model.*;

public final class SetupService {
    private SetupService(){}

    /** Intro/base starting principality exactly like the legacy layout shown by the user. */
    public static void seedIntroPrincipality(Player p){
        // Default to Player 1 layout for backward compatibility
        seedIntroPrincipality(p, 1);
    }

    /** Intro/base starting principality with explicit dice layout per player number (1 or 2). */
    public static void seedIntroPrincipality(Player p, int playerNumber){
        Principality pr = p.principality();

        // Player-specific dice per requirement
        int dBrick, dGold, dLumber, dOre, dWheat, dWool;
        if (playerNumber == 2) {
            // Player 2 mapping
            dBrick = 3; dGold = 1; dLumber = 2; dOre = 5; dWheat = 6; dWool = 4;
        } else {
            // Player 1 mapping (default)
            dBrick = 2; dGold = 4; dLumber = 3; dOre = 6; dWheat = 5; dWool = 1;
        }

        // Top row regions (row 1): Forest (Lumber), Gold, Field (Wheat)
        var r10 = new RegionTile(Resource.WOOD, dLumber); r10.setStored(1); pr.placeRegionTop(0, r10);
        var r12 = new RegionTile(Resource.GOLD, dGold);   r12.setStored(0); pr.placeRegionTop(2, r12);
        var r14 = new RegionTile(Resource.WHEAT, dWheat); r14.setStored(1); pr.placeRegionTop(4, r14);

        // Bottom row regions (row 3): Hill (Brick), Pasture (Wool), Mountain (Ore)
        var r30 = new RegionTile(Resource.BRICK, dBrick); r30.setStored(1); pr.placeRegionBottom(0, r30);
        var r32 = new RegionTile(Resource.WOOL, dWool);   r32.setStored(1); pr.placeRegionBottom(2, r32);
        var r34 = new RegionTile(Resource.ORE, dOre);     r34.setStored(1); pr.placeRegionBottom(4, r34);

        // Centers row 2 at columns 1 (left), 2 (road), 3 (right)
        pr.placeSettlementAt(2, 1);
        pr.placeRoadAt(2, 2);
        pr.placeSettlementAt(2, 3);
        // Starting VP: 2 settlements = 2 VP
        pr.getPoints().addVP(2);
    }
}
