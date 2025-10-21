# Unit Tests for Basic Set Cards - Summary

This document summarizes the unit tests created for all cards in the basic set.

## Tests Created

### Building Cards

1. **AbbeyTest.java** - Tests that Abbey provides 1 Progress Point (PP)
2. **MarketplaceTest.java** - Tests that Marketplace provides 1 Commerce Point (CP)
3. **TollBridgeTest.java** - Tests that Toll Bridge provides 1 CP
4. **ProductionBoostersTest.java** - Tests placement of production booster buildings:
   - Iron Foundry (doubles ore from mountains)
   - Grain Mill (doubles grain from fields)
   - Lumber Camp (doubles lumber from forests)
   - Brick Factory (doubles brick from hills)
   - Weaver's Shop (doubles wool from pastures)
5. **StorehouseTest.java** - Tests Storehouse placement

### Hero Cards

6. **HeroesTest.java** - Tests all heroes provide correct Strength Points (SP) and Skill Points (FP):
   - Austin: 1 SP, 2 FP
   - Harald: 2 SP, 1 FP
   - Inga: 1 SP, 3 FP
   - Osmund: 2 SP, 2 FP
   - Candamir: 4 SP, 1 FP
   - Siglind: 2 SP, 3 FP

### Trade Ship Cards

7. **TradeShipsTest.java** - Tests all trade ships provide 1 Commerce Point (CP):
   - Large Trade Ship
   - Gold Ship
   - Ore Ship
   - Grain Ship
   - Lumber Ship
   - Brick Ship
   - Wool Ship

### Action Cards

8. **RelocationTest.java** - Tests Relocation card effect (allows changing position of two regions/buildings)

### Event Cards

9. **EventCardsTest.java** - Tests all event card effects:
   - Invention (each player gets 1 resource per building with PP, max 2)
   - Yule (shuffle event stack and draw another event)
   - Year of Plenty (each region gets 1 resource per adjacent Storehouse/Abbey)
   - Fraternal Feuds (player with strength advantage selects 2 cards from opponent's hand)
   - Feud (player with strength advantage selects 3 opponent buildings; opponent removes 1)
   - Traveling Merchant (each player may take up to 2 resources, paying 1 gold per resource)
   - Trade Ships Race (player with most trade ships receives 1 resource)

## Existing Tests (Already Present)

The following cards already had tests in the repository:
- Brigitta, the Wise Woman (BrigittaPreRollTest.java)
- Scout (ScoutCardTest.java)
- Merchant Caravan (MerchantCaravanHappyTest.java, MerchantCaravanDoubleDiscardTest.java)
- Goldsmith (GoldsmithHappyTest.java, GoldsmithInsufficientTest.java)
- Parish Hall (ParishHallExchangeDiscountTest.java)

## Test Execution

All newly created tests pass successfully:

```bash
# Compile all new tests
javac -cp ".:gson.jar:pom.xml" src/tests/*.java

# Run individual tests
java -cp ".:gson.jar:pom.xml" src.tests.AbbeyTest
java -cp ".:gson.jar:pom.xml" src.tests.HeroesTest
java -cp ".:gson.jar:pom.xml" src.tests.TradeShipsTest
java -cp ".:gson.jar:pom.xml" src.tests.MarketplaceTest
java -cp ".:gson.jar:pom.xml" src.tests.TollBridgeTest
java -cp ".:gson.jar:pom.xml" src.tests.ProductionBoostersTest
java -cp ".:gson.jar:pom.xml" src.tests.StorehouseTest
java -cp ".:gson.jar:pom.xml" src.tests.RelocationTest
java -cp ".:gson.jar:pom.xml" src.tests.EventCardsTest
```

## Test Design Notes

### Point-Based Cards
For cards that provide static points (Abbey, Heroes, Trade Ships, Marketplace, Toll Bridge), tests verify that the appropriate points are added to the Principality's Points object.

### Production Boosters
Production booster buildings (Iron Foundry, Grain Mill, Lumber Camp, Brick Factory, Weaver's Shop) are tested for correct placement at building sites. The actual production doubling effect is implemented in DeckManager's private `applyProductionBoosters` method and would require integration testing with the full production system.

### Action and Event Cards
Action and event cards are tested by verifying that their effect implementations can be invoked successfully and produce appropriate output or state changes.

### Integration vs Unit Testing
Some card effects (e.g., Storehouse's Brigand Attack protection, Marketplace's production-based resource gain, production doubling) require integration with the event or production systems. These are noted in the test files as requiring integration testing, while the unit tests focus on verifiable behaviors like placement, point provision, and effect invocation.

## Coverage

All cards mentioned in the issue requirements now have unit tests:

✅ Toll Bridge  
✅ Storehouse  
✅ Iron Foundry  
✅ Grain Mill  
✅ Lumber Camp  
✅ Brick Factory  
✅ Weaver's Shop  
✅ Abbey  
✅ Marketplace  
✅ Parish Hall (existing)  
✅ Large Trade Ship  
✅ Gold Ship  
✅ Ore Ship  
✅ Grain Ship  
✅ Lumber Ship  
✅ Brick Ship  
✅ Wool Ship  
✅ Austin  
✅ Harald  
✅ Inga  
✅ Osmund  
✅ Candamir  
✅ Siglind  
✅ Brigitta, The Wise Woman (existing)  
✅ Relocation  
✅ Scout (existing)  
✅ Merchant Caravan (existing)  
✅ Goldsmith (existing)  
✅ Invention  
✅ Yule  
✅ Year of Plenty  
✅ Fraternal Feuds  
✅ Feud  
✅ Traveling Merchant  
✅ Trade Ships Race  
