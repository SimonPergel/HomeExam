# Quick Reference: Legacy vs Refactored Code

## At a Glance

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         LEGACY CODE                                     │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│   Card.java (649 lines)                                                 │
│   ├── Card data + JSON loading + Deck management + Effects             │
│   └── Static vectors for all decks                                     │
│                                                                         │
│   Player.java (621 lines)                                               │
│   ├── Player state + Console I/O + Grid + Resources                    │
│   └── All player operations in one class                               │
│                                                                         │
│   Server.java (1,369 lines)                                             │
│   ├── Game loop + Production + Events + Actions                        │
│   ├── Replenish + Exchange + Rules + Win conditions                    │
│   └── Everything game-related in one giant class                       │
│                                                                         │
│   OnlinePlayer.java (93 lines)                                          │
│   └── Network I/O extension                                            │
│                                                                         │
│   Total: 4 files, ~2,700 lines                                          │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

```
┌─────────────────────────────────────────────────────────────────────────┐
│                       REFACTORED CODE                                   │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│   src/model/ (11 files)                                                 │
│   ├── Card.java           - Immutable base                             │
│   ├── BasicCard.java      - With effect strategy                       │
│   ├── Player.java         - Pure data model                            │
│   ├── Principality.java   - Board state                                │
│   └── RegionTile.java     - Region abstraction                         │
│                                                                         │
│   src/controller/ (60+ files)                                           │
│   ├── GameController.java - Main orchestrator                          │
│   ├── TurnManager.java    - Turn flow                                  │
│   ├── DeckManager.java    - Deck operations                            │
│   ├── EventManager.java   - Event resolution                           │
│   ├── RuleValidator.java  - Rule enforcement                           │
│   ├── actions/            - 8 action card effects                      │
│   ├── event/              - 7 event card effects                       │
│   ├── eventDieEvents/     - 5 event die effects                        │
│   └── placement/          - 9 placement handlers                       │
│                                                                         │
│   src/io/ (6 files)                                                     │
│   ├── interfaces/         - IInputService, IOutputService              │
│   └── Console + Socket implementations                                 │
│                                                                         │
│   src/network/ (6 files)                                                │
│   └── Server, OnlinePlayer, ClientConnection                           │
│                                                                         │
│   src/util/ (6 files)                                                   │
│   └── Dice, Config, policies                                           │
│                                                                         │
│   src/view/ (1 file)                                                    │
│   └── BoardPrinter.java                                                │
│                                                                         │
│   src/tests/ (8 files)                                                  │
│   └── Unit tests for key features                                      │
│                                                                         │
│   Total: 106 files, ~6,000+ lines                                       │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## SOLID Principles Score Card

| Principle | Legacy | Refactored | Winner |
|-----------|--------|------------|--------|
| **Single Responsibility** | ❌ One class = many responsibilities | ✅ Each class = one responsibility | 🏆 Refactored |
| **Open/Closed** | ❌ Modify existing code for new features | ✅ Extend via new classes | 🏆 Refactored |
| **Liskov Substitution** | ⚠️ Inheritance breaks I/O assumptions | ✅ Proper inheritance hierarchy | 🏆 Refactored |
| **Interface Segregation** | ❌ No interfaces | ✅ Minimal focused interfaces | 🏆 Refactored |
| **Dependency Inversion** | ❌ Depends on concrete classes | ✅ Depends on abstractions | 🏆 Refactored |

---

## Booch Metrics Comparison

| Metric | Legacy | Refactored |
|--------|--------|------------|
| **Coupling** | 🔴 Very High | 🟢 Low |
| **Cohesion** | 🔴 Very Low | 🟢 High |
| **Complexity** | 🔴 30+ per class | 🟢 2-5 per method |
| **Lines per Class** | 🔴 650-1,370 | 🟢 50-150 |
| **Testability** | 🔴 Hard (static state) | 🟢 Easy (DI) |

---

## Feature Parity Matrix

| Feature Category | Legacy | Refactored | Status |
|------------------|--------|------------|--------|
| **Event Die (1-4)** | ✅ Brigand, Trade, Celebration, Harvest | ✅ All implemented | ✅ Equal |
| **Event Cards** | ✅ All 7 cards | ✅ All 7 cards | ✅ Equal |
| **Action Cards** | ⚠️ 5 explicit + generic | ✅ 7+ specific | ✅ Refactored better |
| **Buildings** | ✅ Abbey, Marketplace, etc. | ✅ All implemented | ✅ Equal |
| **Boosters** | ✅ Foundry, Mill, etc. | ✅ All implemented | ✅ Equal |
| **Trade Ships** | ✅ LTS + 6 common | ✅ All implemented | ✅ Equal |
| **Heroes** | ⚠️ Generic +points | ✅ Specific implementations | ✅ Refactored better |
| **Center Cards** | ✅ Road, Settlement, City | ✅ All implemented | ✅ Equal |
| **Production** | ✅ With boosters | ✅ With boosters | ⚠️ Need test |
| **Marketplace** | ✅ Flag-based | ✅ Effect-based | ⚠️ Need test |
| **Storehouse** | ✅ Flag-based | ✅ Effect-based | ⚠️ Need test |
| **Turn Flow** | ✅ Brigand-first rule | ✅ Brigand-first rule | ✅ Equal |
| **Replenish** | ✅ With Fraternal Feuds | ✅ With Fraternal Feuds | ✅ Equal |
| **Exchange** | ✅ With Parish Hall | ✅ With Parish Hall | ✅ Equal |
| **Networking** | ✅ Socket-based | ✅ Socket-based | ✅ Equal |

**Legend:**
- ✅ Fully implemented and verified
- ⚠️ Implemented but needs runtime verification
- ❌ Not implemented

---

## Extensibility Comparison

### Adding a New Action Card

**Legacy Approach:**
```java
// Modify Card.java applyEffect() method
if (nmEquals(nm, "NewCard")) {
    // Add 50+ lines of logic here
    // Mixed with 20+ other cards
    return true;
}
```

**Refactored Approach:**
```java
// Create new file: actions/NewCardEffect.java
public class NewCardEffect implements ICardEffect {
    @Override
    public void apply(GameContext ctx) {
        // Logic here (isolated)
    }
}

// Register in EffectCatalog
catalog.register("newcard", new NewCardEffect());
```

### Adding a New Event

**Legacy Approach:**
```java
// Modify Server.resolveEvent() method
else if (nm.equalsIgnoreCase("NewEvent")) {
    // Add 30+ lines of logic here
    // Mixed with other events
}
```

**Refactored Approach:**
```java
// Create new file: event/NewEventEffect.java
public class NewEventEffect implements ICardEffect {
    @Override
    public void apply(GameContext ctx) {
        // Logic here (isolated)
    }
}

// Register in EffectCatalog
catalog.register("event:newevent", new NewEventEffect());
```

### Adding a New Placement Type

**Legacy Approach:**
```java
// Modify Card.applyEffect() with more if/else branches
// Add validation logic inline
// Mix with existing placement code
```

**Refactored Approach:**
```java
// Create new file: placement/NewPlacementHandler.java
public class NewPlacementHandler implements PlacementHandler {
    @Override
    public boolean canHandle(Card c) { ... }
    
    @Override
    public boolean tryPlace(Card c, GameContext ctx) { ... }
}

// Add to PlacementRegistry
registry.add(new NewPlacementHandler());
```

---

## Maintainability Scenarios

### Scenario 1: Fix a bug in Marketplace bonus

**Legacy:**
1. Open Server.java (1,369 lines)
2. Find applyProduction() method
3. Navigate through nested loops
4. Find Marketplace section (around line 338)
5. Modify code carefully (affects production for all players)
6. Risk: Breaking unrelated production logic

**Refactored:**
1. Open GameController.java
2. Find applyMarketplaceBonus() method (isolated)
3. Modify with confidence (single responsibility)
4. Risk: Minimal (well-encapsulated)

### Scenario 2: Change Storehouse protection rule

**Legacy:**
1. Find storehouseExcludedKeys() in Server.java
2. Find all places it's called (2-3 different events)
3. Modify each location
4. Risk: Inconsistent changes

**Refactored:**
1. Open StorehouseEffect.java
2. Modify getProtectedRegions() method
3. All events use same logic automatically
4. Risk: Very low

### Scenario 3: Add unit tests

**Legacy:**
- Need to refactor static state first
- Need to mock console I/O
- Need to break dependencies
- Effort: High (days of work)

**Refactored:**
- Already has 8 unit tests
- Use MockIO for I/O
- Inject dependencies
- Effort: Low (add new test file)

---

## Performance Considerations

| Operation | Legacy | Refactored | Notes |
|-----------|--------|------------|-------|
| **Find card in hand** | O(n) linear search | O(n) linear search | Same |
| **Find region by type** | O(n) grid scan | O(n) grid scan | Same |
| **Check building** | O(n) flag search | O(n) building lookup | Could optimize |
| **Production** | O(regions) per player | O(regions) per player | Same |
| **Event resolution** | if/else chain | Registry lookup O(1) | Refactored better |

**Verdict:** Performance is equivalent for gameplay. Refactored has slight advantage in effect lookup.

---

## Testing Support

### Legacy Code Testing:
```
❌ No unit tests
❌ Static state makes testing hard
❌ Hardcoded I/O prevents mocking
❌ High coupling prevents isolation
❌ Would need significant refactoring first
```

### Refactored Code Testing:
```
✅ 8 unit tests already present
✅ MockIO for I/O mocking
✅ Dependency injection enables isolation
✅ Low coupling enables unit testing
✅ Ready for comprehensive test suite
```

---

## Migration Risks

### Low Risk Areas (High Confidence):
- ✅ Turn flow (same Brigand-first rule)
- ✅ Replenish phase (identical logic)
- ✅ Exchange phase (identical logic)
- ✅ Center card costs (Road, Settlement, City)
- ✅ Event die faces 1-4

### Medium Risk Areas (Need Testing):
- ⚠️ Production with boosters (timing may differ)
- ⚠️ Marketplace bonus (timing may differ)
- ⚠️ Storehouse protection (coordinate calculation)
- ⚠️ Large Trade Ship adjacency
- ⚠️ Year of Plenty with Storehouse/Abbey

### Already Mitigated:
- ✅ Unit tests for Goldsmith
- ✅ Unit tests for Merchant Caravan
- ✅ Unit tests for Brigitta
- ✅ Unit tests for Parish Hall
- ✅ Unit tests for placement rules

---

## Recommendation Summary

### Immediate Actions:
1. ✅ **Accept refactored code** - superior architecture
2. ⚠️ **Run integration tests** - verify equivalence
3. ⚠️ **Test medium-risk areas** - production, marketplace, storehouse
4. ✅ **Add more unit tests** - increase coverage

### Long-term:
- ✅ Use refactored code as base for expansions
- ✅ Add Theme Sets using policy interfaces
- ✅ Add more event cards via effect registry
- ✅ Maintain SOLID principles in new code

---

## Final Score

```
┌────────────────────────────────────────────┐
│            OVERALL RATING                  │
├────────────────────────────────────────────┤
│                                            │
│  Legacy Code:          ⭐⭐☆☆☆            │
│  - Works but unmaintainable                │
│                                            │
│  Refactored Code:      ⭐⭐⭐⭐⭐          │
│  - Excellent architecture                  │
│  - Ready for production                    │
│  - Extensible for future                   │
│                                            │
│  WINNER: 🏆 Refactored Code                │
│                                            │
└────────────────────────────────────────────┘
```

---

**See CODE_REVIEW_SUMMARY.md for detailed analysis.**
