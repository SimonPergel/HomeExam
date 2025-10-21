# Incomplete Card Implementations in Refactored Code

**Analysis Date:** October 21, 2025  
**Requested by:** @SimonPergel

## Summary

After examining all card effect implementations in the refactored code (`src/controller/`), I identified **1 incomplete action card** and several architectural patterns to note.

---

## ❌ Incomplete Implementation

### 1. **Scout** (Action Card)
**File:** `src/controller/actions/Scout.java`  
**Status:** ❌ **STUB - No Implementation**

**Current State:**
```java
package src.controller.actions;
import src.controller.ICardEffect;
public class Scout {
    // Empty class - no implementation
}
```

**Legacy Implementation:** Card.java lines 523-528, 267-290
- Adds "SCOUT_NEXT_SETTLEMENT" flag when played
- Allows player to choose 2 specific regions (by name or index) from region stack
- Regions are selected when next Settlement is built
- Full interactive prompts for region selection

**What's Missing:**
- Does NOT implement ICardEffect interface
- No apply() method
- No logic to set flag for settlement placement
- No integration with settlement region placement

**Impact:** High - Scout is a Basic set action card that should be playable

---

## ✅ Complete Implementations

### Action Cards (All Complete Except Scout)

| Card Name | File | Status | Lines | Notes |
|-----------|------|--------|-------|-------|
| **Merchant Caravan** | MerchantCaravanEffect.java | ✅ Complete | 196 | Full implementation with preconditions |
| **Goldsmith** | GoldsmithEffect.java | ✅ Complete | 98 | Full implementation with preconditions |
| **Brigitta the Wise** | BrigittaTheWiseEffect.java | ✅ Complete | 33 | Pre-roll effect, sets production override |
| **Relocation** | RelocationEffect.java | ✅ Complete | 39 | Swaps regions or expansions |
| **Road Building** | RoadBuildingEffect.java | ✅ Complete | 48 | Builds road from hand |
| **Merchant** | MerchantEffect.java | ✅ Complete | 13 | Gains 2 of chosen resource |
| **Scout** | Scout.java | ❌ **STUB** | 5 | **INCOMPLETE** |

### Event Cards (All Complete)

| Card Name | File | Status | Lines |
|-----------|------|--------|-------|
| **Feud** | FeudEffect.java | ✅ Complete | 157 |
| **Fraternal Feuds** | FraternalFeudsEffect.java | ✅ Complete | 79 |
| **Invention** | InventionEventEffect.java | ✅ Complete | 63 |
| **Trade Ships Race** | TradeShipsRaceEffect.java | ✅ Complete | 22 |
| **Traveling Merchant** | TravelingMerchantEffect.java | ✅ Complete | 17 |
| **Year of Plenty** | YearOfPlentyEffect.java | ✅ Complete | 65 |
| **Yule** | YuleEffect.java | ✅ Complete | 24 |

### Event Die Effects (All Complete)

| Face | Effect | File | Status | Lines |
|------|--------|------|--------|-------|
| 1 | Brigand | BrigandEffect.java | ✅ Complete | 52 |
| 2 | Trade | TradeEffect.java | ✅ Complete | 53 |
| 3 | Celebration | CelebrationEffect.java | ✅ Complete | 60 |
| 4 | Plentiful Harvest | HarvestEffect.java (actions) | ✅ Complete | 108 |
| 5-6 | Event Card | EventCardEffect.java | ✅ Complete | 12 |

### Building Effects (All Complete)

| Building | File | Status | Lines | Notes |
|----------|------|--------|-------|-------|
| **Abbey** | AbbeyEffect.java | ✅ Complete | 13 | +1 Progress Point |
| **Marketplace** | MarketplaceEffect.java | ✅ Complete | 21 | +1 Commerce, passive effect in GameController |
| **Parish Hall** | (Handler only) | ✅ Complete | N/A | Discount handled in DeckManager |
| **Storehouse** | StorehouseEffect.java | ✅ Complete | 16 | Brigand protection |
| **Toll Bridge** | TollBridgeEffect.java | ✅ Complete | 21 | +1 Commerce, Harvest bonus |
| **Booster Buildings** | (Empty lambdas) | ✅ Complete | N/A | Handled in Principality production |

**Note on Boosters:** Iron Foundry, Grain Mill, Lumber Camp, Brick Factory, Weaver's Shop are registered with empty lambda functions `() -> ctx -> {}` because their effects are passive and handled during production phase in the Principality class, not as active card effects.

### Unit Effects (All Complete)

| Unit | File | Status | Lines |
|------|------|--------|-------|
| **Common Heroes** (6) | CommonHeroEffect.java | ✅ Complete | 29 |
| **Common Trade Ships** (6) | CommonTradeShipEffect.java | ✅ Complete | 30 |
| **Large Trade Ship** | LargeTradeShipEffect.java | ✅ Complete | 19 |

---

## 📊 Architecture Notes

### Files That Are Intentionally Not Full Classes

1. **PreRollEffect.java** (4 lines)
   - **Purpose:** Marker interface, not a bug
   - Used to identify effects that can be played before dice roll
   - Brigitta implements this interface

2. **eventDieEvents/HarvestEffect.java** (1 line)
   - **Purpose:** Deprecated duplicate reference
   - Actual implementation is in `actions/HarvestEffect.java`
   - Just a comment redirecting to the correct file

3. **Booster Buildings** (registered as empty lambdas)
   - **Purpose:** Passive effects handled in production
   - Not action effects, so empty registration is correct
   - Logic is in `Principality.produce()` method

---

## 🔍 Effect Registration Status

Checked `EffectCatalog.java` to see what's registered:

### ✅ Registered and Complete:
- merchant caravan
- goldsmith  
- brigitta (the wise woman)
- relocation
- road building
- merchant
- All event cards (7)
- All event die faces (5)
- All heroes (6)
- All trade ships (7)
- All buildings (5 + 5 boosters)

### ❌ NOT Registered:
- **scout** - Not in EffectCatalog and class is empty stub

---

## 📝 Detailed Scout Comparison

### Legacy Implementation (Card.java)

**When Scout is played (lines 523-528):**
```java
if (nmEquals(nm, "Scout")) {
    // Only meaningful when used with a new settlement
    active.flags.add("SCOUT_NEXT_SETTLEMENT");
    return true;
}
```

**When Settlement is built (lines 267-290):**
```java
if (active.flags.contains("SCOUT_NEXT_SETTLEMENT")) {
    // SCOUT: let player pick two specific regions from stack
    active.sendMessage("PROMPT: SCOUT - Choose first region (name or index):");
    String s1 = active.receiveMessage();
    first = pickRegionFromStackByNameOrIndex(s1);
    // ... (fallback if null)
    
    active.sendMessage("PROMPT: SCOUT - Choose second region (name or index):");
    String s2 = active.receiveMessage();
    second = pickRegionFromStackByNameOrIndex(s2);
    // ... (fallback if null)
    
    // Clear flag after use
    active.flags.remove("SCOUT_NEXT_SETTLEMENT");
}
```

### Refactored Implementation Status

**Current:** Empty stub class
**Needs:** 
1. Implement ICardEffect interface
2. Add apply() method that sets a flag or state in TurnState/Player
3. Integrate with SettlementPlacementHandler to check for Scout flag
4. Prompt player to choose 2 regions from deck when flag is set
5. Clear flag after settlement placement

---

## 🎯 Recommendation

### Immediate Action Required

**Scout card needs implementation:**

1. **Implement Scout.java** to:
   - Implement ICardEffect interface
   - Set a flag/state indicating Scout was played
   - Integrate with settlement placement flow

2. **Register in EffectCatalog.java:**
   ```java
   .register("scout", Scout::new)
   ```

3. **Modify SettlementPlacementHandler** to:
   - Check if Scout effect is active
   - Allow player to choose 2 regions instead of taking top 2
   - Clear Scout state after settlement placement

### Testing Priority

After implementing Scout:
- Unit test: Scout sets appropriate flag/state
- Integration test: Scout allows region selection during settlement
- Runtime test: Play Scout, build settlement, verify region choice

---

## 📚 Additional Context

### Why This Matters

Scout is listed in `cards.json` as a Basic set Action card:
```json
{
  "name": "Scout",
  "type": "Action – Neutral",
  "placement": "Action",
  "cardText": "Play this card when building a settlement. Take 2 cards of your choice from the region card stack."
}
```

It's not an edge case or expansion card—it's part of the core Basic introductory game. The fact that it's a stub means the refactored version is **functionally incomplete** compared to the legacy version.

---

## Summary Table

| Category | Total | Complete | Incomplete | Completion % |
|----------|-------|----------|------------|--------------|
| **Action Cards** | 7 | 6 | 1 | 85.7% |
| **Event Cards** | 7 | 7 | 0 | 100% |
| **Event Die Effects** | 5 | 5 | 0 | 100% |
| **Building Effects** | 10 | 10 | 0 | 100% |
| **Unit Effects** | 13 | 13 | 0 | 100% |
| **TOTAL** | 42 | 41 | 1 | **97.6%** |

---

**Conclusion:** The refactored code is **97.6% complete** in terms of card implementations. Only **Scout** action card is incomplete (stub with no implementation). All other cards have full implementations that match or exceed the legacy functionality.
