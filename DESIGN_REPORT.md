# Design Report: Rivals for Catan - Refactored Architecture

## Executive Summary

This document provides a comprehensive analysis of the refactored architecture for the Rivals for Catan card game implementation. The design follows SOLID principles, maintains good Booch metrics, and explicitly addresses key quality attributes including **modifiability**, **extensibility**, **testability**, and **maintainability**. The architecture is structured to support the base game rules while being prepared for future expansions and different eras without requiring modifications to core components.

---

## 1. SOLID Principles Analysis

### 1.1 Single Responsibility Principle (SRP)

Each class in the system has a single, well-defined responsibility:

**Model Layer:**
- `Player`: Manages player state (resources, hand, victory points, advantages)
- `Principality`: Manages the player's board (regions, settlements, cities, roads, buildings)
- `Card` hierarchy (`Card`, `BasicCard`, `CenterCard`, `EventCard`): Represent different card types with their specific attributes
- `RegionTile`: Manages individual region state (resource type, dice number, stored resources)
- `Points`: Tracks different point types (VP, CP, SP, FP, PP) separately

**Controller Layer:**
- `GameController`: Orchestrates game flow and coordinates between components
- `TurnManager`: Manages the sequence of a single turn (pre-roll, production, events, actions)
- `DeckManager`: Manages all card decks and drawing/shuffling operations
- `EventManager`: Handles event die rolls and event resolution
- `RuleValidator`: Validates game rules (costs, victory conditions, prerequisites)
- `CardFactory`: Loads and creates cards from JSON configuration
- `EffectCatalog`: Registry mapping effect names to effect implementations
- `SetupService`: Handles initial game setup

**Effect Layer:**
- Each effect class (e.g., `BrigandEffect`, `GoldsmithEffect`, `RelocationEffect`) handles exactly one card's behavior
- Placement handlers (e.g., `RoadPlacementHandler`, `SettlementPlacementHandler`) handle placement logic for specific card types

**I/O Layer:**
- `IInputService`/`IOutputService`: Define input/output contracts
- `ConsoleInput`/`ConsoleOutput`: Handle console-based I/O
- `SocketInput`/`SocketOutput`: Handle network-based I/O
- `MockIO`: Provides test doubles for automated testing

**Design Rationale:**
By separating concerns, each class can be understood, tested, and modified independently. For example, changing how production works only requires modifying `DeckManager.produce()`, not the entire game flow.

### 1.2 Open/Closed Principle (OCP)

The design is open for extension but closed for modification:

**Extension Points:**

1. **Effect System (`EffectCatalog` + `ICardEffect`):**
   ```java
   // Adding a new card effect requires NO modification to existing code
   public class NewCardEffect implements ICardEffect {
       @Override
       public void apply(GameContext ctx) {
           // New behavior here
       }
   }
   
   // Register in EffectCatalog builder
   EffectCatalog.builder()
       .register("new card", NewCardEffect::new)
       .build();
   ```

2. **Placement System (`PlacementRegistry` + `PlacementHandler`):**
   ```java
   // Adding new placement logic without modifying PlacementRegistry
   public class NewPlacementHandler implements PlacementHandler {
       @Override
       public boolean canHandle(BasicCard card) { /* ... */ }
       
       @Override
       public boolean place(BasicCard card, GameContext ctx) { /* ... */ }
   }
   ```

3. **Policy Interfaces (`VictoryPolicy`, `ProductionPolicy`, `PlacementPolicy`, `TradePolicy`):**
   - Future eras/expansions can provide alternative policy implementations
   - The core game logic delegates to these policies without knowing the specifics
   - Example: Different victory conditions for different eras

4. **I/O Abstraction:**
   - New I/O mechanisms (e.g., GUI, web interface) can be added by implementing `IInputService` and `IOutputService`
   - No changes needed to game logic

**Design Rationale:**
The Strategy pattern (via interfaces) and Registry pattern (via `EffectCatalog` and `PlacementRegistry`) allow new behaviors to be added without modifying tested code, reducing regression risk.

### 1.3 Liskov Substitution Principle (LSP)

Subtypes can be substituted for their base types without breaking correctness:

**Card Hierarchy:**
- `BasicCard`, `CenterCard`, and `EventCard` all extend `Card`
- Each subtype adds specialized behavior without violating base class contracts
- `Deck<T>` is generic and works correctly with any card subtype

**I/O Implementations:**
- `ConsoleInput`, `SocketInput`, and `MockIO` all implement `IInputService`
- Any code using `IInputService` works correctly with any implementation
- Tests use `MockIO`; production uses `ConsoleInput` or `SocketInput`

**Effect Implementations:**
- All effect classes implement `ICardEffect`
- `EffectCatalog` and `GameContext` work with any `ICardEffect` implementation
- Adding new effects doesn't require changes to effect consumers

**Design Rationale:**
Proper abstractions ensure that polymorphism works correctly. This is critical for testability (mock objects) and extensibility (new implementations).

### 1.4 Interface Segregation Principle (ISP)

Interfaces are minimal and focused on specific client needs:

**I/O Interfaces:**
- `IInputService`: Only input operations (`readLine()`, `readInt()`, etc.)
- `IOutputService`: Only output operations (`print()`, `println()`)
- `IPlayerIO`: Combines input and output for player-specific I/O
- Clients depend only on what they need

**Controller Interfaces:**
- `IDeckProvider`: Provides access to decks
- `IEventHandler`: Handles event resolution
- `IRuleValidator`: Validates rules
- `IGameController`: Coordinates game flow

**Effect Interface:**
- `ICardEffect`: Single method `apply(GameContext ctx)`
- Effects don't need to know about parsing, validation, or I/O
- All necessary context is provided via `GameContext`

**Design Rationale:**
Small, focused interfaces prevent classes from depending on methods they don't use. This reduces coupling and makes the system more flexible.

### 1.5 Dependency Inversion Principle (DIP)

High-level modules depend on abstractions, not concretions:

**Dependency Flow:**
```
GameController (high-level)
    ↓ depends on ↓
IInputService, IOutputService, IDeckProvider (abstractions)
    ↑ implemented by ↑
ConsoleInput, ConsoleOutput, DeckManager (low-level)
```

**Key Examples:**

1. **GameController Dependencies:**
   ```java
   public GameController(TurnManager t, DeckManager d, RuleValidator r,
                         Randomizer rng, GameConfig cfg,
                         IInputService in, IOutputService out)
   ```
   - Depends on abstractions (`IInputService`, `IOutputService`)
   - Concrete implementations injected at construction
   - Enables testing with `MockIO` and production with `ConsoleInput`/`ConsoleOutput`

2. **Effect System:**
   - Effects depend on `GameContext` (abstraction)
   - `GameContext` provides access to necessary services via interfaces
   - Effects don't directly instantiate services

3. **Configuration:**
   - `GameConfig` is passed to validators and managers
   - Future eras can extend or replace `GameConfig`
   - Core logic depends on the abstraction, not specific values

**Design Rationale:**
Dependency injection and abstraction-based design make the system testable, flexible, and maintainable. Components can be replaced or mocked without changing dependent code.

---

## 2. Booch's Metrics Analysis

### 2.1 Coupling (Low is Better)

The architecture maintains **low coupling** through several mechanisms:

**Achieved Through:**

1. **Interface-Based Design:**
   - Components communicate through well-defined interfaces
   - Example: `GameController` uses `IInputService`, not `ConsoleInput` directly
   - Change in concrete implementation doesn't affect clients

2. **Dependency Injection:**
   - Dependencies are injected rather than created internally
   - Example: `GameController` receives `DeckManager`, doesn't create it
   - Reduces compile-time and runtime dependencies

3. **Event-Driven Communication:**
   - `EventManager` coordinates event handling without tight coupling to specific effects
   - Effects register via `EffectCatalog`, not hard-coded references

4. **Package Organization:**
   - Clear package boundaries: `model`, `controller`, `io`, `network`, `util`, `view`
   - Each package has minimal dependencies on others
   - `model` has no dependencies on `controller` or `io`

**Metrics:**
- **Afferent Coupling (Ca)**: Number of classes outside a package that depend on classes inside
  - `model`: High Ca (used by many), appropriate for core domain
  - `controller.actions`: Low Ca (used only by `EffectCatalog`)
  
- **Efferent Coupling (Ce)**: Number of classes inside a package that depend on classes outside
  - `model`: Low Ce (minimal dependencies)
  - `controller`: Moderate Ce (coordinates many components)

**Benefits:**
- Changes in one component have minimal ripple effects
- Components can be tested in isolation
- Easier to understand and reason about individual components

### 2.2 Cohesion (High is Better)

The architecture maintains **high cohesion** through focused responsibilities:

**Examples of High Cohesion:**

1. **Controller Package:**
   - All classes relate to game control and orchestration
   - `TurnManager`, `GameController`, `EventManager` work together to manage game flow
   
2. **Model Package:**
   - All classes represent domain concepts
   - `Player`, `Card`, `Principality`, `Resource` are pure domain objects
   
3. **Effect Packages:**
   - `controller.actions`: Action card effects
   - `controller.event`: Event card effects
   - `controller.eventDieEvents`: Event die effects
   - `controller.settlement`: Settlement/City expansion effects
   
4. **Placement Package:**
   - All classes relate to placing cards on the board
   - Placement handlers, registry, and coordinate prompting
   
5. **I/O Package:**
   - All classes handle input/output
   - Console, socket, and mock implementations grouped together

**Metrics:**
- **Lack of Cohesion in Methods (LCOM)**: Low for most classes
  - Methods in each class operate on the same data
  - Example: `Player` methods all manipulate player state
  
**Benefits:**
- Classes are easier to understand and maintain
- Changes to requirements typically affect only one module
- Easier to locate functionality

### 2.3 Complexity (Low is Better)

The design minimizes complexity through clear structure:

**Complexity Management Strategies:**

1. **Separation of Concerns:**
   - Turn flow separated from card effects
   - Effect resolution separated from validation
   - I/O separated from game logic

2. **Small, Focused Classes:**
   - Most effect classes are < 100 lines
   - Single-purpose handlers and validators
   - Clear method names and responsibilities

3. **Encapsulation:**
   - Internal complexity hidden behind simple interfaces
   - Example: `EffectCatalog` hides effect registration complexity
   - Example: `PlacementRegistry` hides handler selection logic

4. **Cyclomatic Complexity:**
   - Most methods have low branching complexity
   - Complex logic (e.g., placement validation) delegated to specialized classes
   - Strategy pattern reduces conditional logic

**Example - Low Complexity:**
```java
public void playTurn(GameController game, Player current, Player opponent) {
    TurnState ts = new TurnState();
    game.setCurrentTurnState(ts);
    
    preRoll.run(game, current, opponent);
    
    int eventFace = eventMgr.rollFace();
    int prodRoll = ts.getProductionOverride().orElse(productionDie.roll(6));
    
    // Clear sequence of operations
    if (eventFace == 1) {
        eventMgr.resolveFace(ctx, game.getDecks(), eventFace);
        game.produce(prodRoll, current, opponent);
    } else {
        game.produce(prodRoll, current, opponent);
        eventMgr.resolveFace(ctx, game.getDecks(), eventFace);
    }
    
    game.actionPhase(current, opponent);
    game.replenish(current);
    game.exchange(current);
}
```

**Benefits:**
- Easier to understand and debug
- Fewer bugs due to simpler logic
- Easier to test (fewer edge cases)

### 2.4 Abstraction (Appropriate Levels)

The design maintains clear abstraction levels:

**Abstraction Hierarchy:**

```
High-Level (Abstract):
├── IGameController, IEventHandler, IRuleValidator (interfaces)
├── GameController, TurnManager (orchestrators)
│
Mid-Level (Business Logic):
├── DeckManager, EventManager, RuleValidator (domain services)
├── EffectCatalog, PlacementRegistry (registries)
│
Low-Level (Implementation):
├── Specific Effects (BrigandEffect, GoldsmithEffect)
├── Specific Handlers (RoadPlacementHandler, SettlementPlacementHandler)
└── I/O Implementations (ConsoleInput, SocketInput)
```

**Appropriate Abstractions:**

1. **Game Flow:** `TurnManager` abstracts turn sequence
2. **Card Effects:** `ICardEffect` abstracts card behavior
3. **I/O Operations:** `IInputService`/`IOutputService` abstract communication
4. **Deck Operations:** `DeckManager` abstracts deck management
5. **Policies:** Policy interfaces abstract variation points

**Benefits:**
- Higher-level code is readable and business-focused
- Lower-level code is reusable and focused on implementation
- Clear separation makes testing easier at each level

---

## 3. Quality Attributes

### 3.1 Modifiability

**Definition:** The ease with which changes can be made to the system.

**Design Decisions Supporting Modifiability:**

1. **Centralized Configuration (`GameConfig`):**
   ```java
   public class GameConfig {
       public int victoryPointsToWin() { return 7; }
       public int centerCards() { return 49; }
       public String roadCost() { return "BBL"; }
       // ... other configuration
   }
   ```
   - **Impact:** Changing game parameters requires modifying only one class
   - **Example:** To change victory points to 10, modify only `GameConfig.victoryPointsToWin()`
   - **Benefit:** No need to search through code for hard-coded values

2. **Effect Registration System:**
   - New cards can be added by implementing `ICardEffect` and registering in `EffectCatalog`
   - No modification to `GameController`, `TurnManager`, or other core components
   - **Example:** Adding "New Era Card" requires only:
     - Create `NewEraCardEffect implements ICardEffect`
     - Register in `EffectCatalog.builder().register("new era card", NewEraCardEffect::new)`

3. **Placement System:**
   - New placement rules can be added via new `PlacementHandler` implementations
   - `PlacementRegistry` automatically uses new handlers
   - **Example:** Adding a new structure type requires only a new handler, not changes to existing code

4. **Policy Interfaces:**
   - `VictoryPolicy`, `ProductionPolicy`, `PlacementPolicy`, `TradePolicy`
   - Different eras can provide different policy implementations
   - Core game logic remains unchanged

5. **Separation of Data and Logic:**
   - Card definitions in `cards.json` (data)
   - Card behavior in effect classes (logic)
   - Changing card costs/names doesn't require code changes

**Scenarios Demonstrating Modifiability:**

| Change Request | Files to Modify | Impact |
|----------------|----------------|---------|
| Change victory points to 10 | `GameConfig.java` | 1 file |
| Add new event card "Earthquake" | Create `EarthquakeEffect.java`, update `EffectCatalog` | 2 files |
| Change production rules | `DeckManager.produce()` | 1 method |
| Add new resource type | `Resource.java`, affected effects | Localized |
| Change card costs | `cards.json` | No code changes |

### 3.2 Extensibility

**Definition:** The ability to add new functionality without modifying existing code.

**Design Decisions Supporting Extensibility:**

1. **Strategy Pattern for Effects:**
   ```java
   public interface ICardEffect {
       void apply(GameContext ctx);
   }
   ```
   - Any new card can provide its own effect implementation
   - `EffectCatalog` registry enables runtime composition
   - **Example Use Case:** Adding "Age of Enlightenment" expansion
     - Create new effect classes for expansion cards
     - Register in a new catalog builder method
     - No changes to existing base game effects

2. **Plugin Architecture for Placement:**
   ```java
   public interface PlacementHandler {
       boolean canHandle(BasicCard card);
       boolean place(BasicCard card, GameContext ctx);
   }
   ```
   - New placement handlers can be added without modifying `PlacementRegistry`
   - **Example Use Case:** Adding ships or special structures
     - Implement new `ShipPlacementHandler`
     - Add to handler list in `GameController` construction
     - Registry automatically delegates to new handler

3. **Extensible I/O Architecture:**
   - `IInputService` and `IOutputService` allow new interfaces
   - **Current Implementations:** Console, Socket, Mock
   - **Future Possibilities:** GUI, web interface, mobile app
   - **Example Use Case:** Adding a graphical interface
     - Implement `GUIInputService` and `GUIOutputService`
     - Inject into `GameController`
     - No changes to game logic

4. **Policy-Based Variation Points:**
   - Different eras can have different rules for:
     - Victory conditions (via `VictoryPolicy`)
     - Production rules (via `ProductionPolicy`)
     - Placement restrictions (via `PlacementPolicy`)
     - Trade mechanics (via `TradePolicy`)
   - **Example Use Case:** "Theme Deck" era with different victory condition
     - Extend `GameConfig` with new victory point calculation
     - Pass to `RuleValidator`
     - No changes to core validation logic

5. **Generic Deck System:**
   ```java
   public class Deck<T extends Card> {
       // Generic operations work with any card type
   }
   ```
   - Can be used with new card types (e.g., `AchievementCard`, `QuestCard`)
   - Type-safe without code duplication

**Extension Scenarios:**

1. **Adding "Age of Darkness" Expansion:**
   - Add new cards to `cards.json` with theme "darkness"
   - Implement new effects (e.g., `CurseEffect`, `NightPhaseEffect`)
   - Register effects in `EffectCatalog`
   - Optionally extend `GameConfig` for new parameters
   - **Result:** Base game code unchanged, expansion code isolated

2. **Adding Network Multiplayer:**
   - Already supported via `network` package
   - `Server`, `ServerHandler`, `ClientConnection`, `OnlinePlayer` provide infrastructure
   - `SocketInput`/`SocketOutput` provide I/O implementations
   - **Result:** Network code separate from game logic

3. **Adding AI Players:**
   - Implement `AIInputService` that uses strategy algorithms
   - Create `AIPlayer` with `bot` flag
   - Inject `AIInputService` for AI player
   - **Result:** No changes to game flow or validation

### 3.3 Testability

**Definition:** The ease with which the system can be tested.

**Design Decisions Supporting Testability:**

1. **Dependency Injection:**
   - All dependencies injected via constructors
   - Easy to inject test doubles (mocks, stubs, fakes)
   - **Example:**
     ```java
     // Production
     GameController gc = new GameController(
         turnManager, deckManager, ruleValidator,
         randomizer, config, consoleInput, consoleOutput
     );
     
     // Testing
     GameController gc = new GameController(
         turnManager, deckManager, ruleValidator,
         mockRandomizer, testConfig, mockInput, mockOutput
     );
     ```

2. **MockIO for Testing:**
   ```java
   MockIO mock = new MockIO();
   mock.queueInput("1", "2", "yes");
   // Run test
   String output = mock.getCapturedOutput();
   ```
   - Captures output for verification
   - Provides scripted input sequences
   - No human interaction needed
   - **Example Tests:** All tests in `src/tests/` use `MockIO`

3. **Small, Focused Classes:**
   - Each effect is a separate class
   - Can test effects in isolation
   - **Example:** `GoldsmithEffect` tested without running full game
   - See `GoldsmithHappyTest.java` and `GoldsmithInsufficientTest.java`

4. **Clear Interfaces:**
   - `ICardEffect` has single method: `apply(GameContext ctx)`
   - Easy to create test implementations
   - Can verify effect behavior without complex setup

5. **Testable Randomness:**
   ```java
   public class Randomizer {
       public int nextInt(int bound) { 
           return random.nextInt(bound); 
       }
   }
   ```
   - Randomness abstracted behind `Randomizer` class
   - Tests can inject seeded `Randomizer` for predictable results
   - **Example:** Set seed to guarantee specific die rolls in tests

6. **Separation of I/O from Logic:**
   - Game logic doesn't perform I/O directly
   - Uses `IInputService`/`IOutputService` abstractions
   - Tests can verify logic without dealing with console/files

**Test Coverage Examples:**

| Component | Test Class | What's Tested |
|-----------|-----------|---------------|
| Goldsmith effect | `GoldsmithHappyTest`, `GoldsmithInsufficientTest` | Resource conversion, cost validation |
| Merchant Caravan | `MerchantCaravanHappyTest`, `MerchantCaravanDoubleDiscardTest` | Card drawing, cost payment |
| Brigitta | `BrigittaPreRollTest` | Pre-roll phase, die control |
| Heroes | `HeroesTest` | Point assignment (SP, FP) |
| Trade Ships | `TradeShipsTest` | Commerce point tracking |
| Abbey | `AbbeyTest` | Progress point provision |
| Event Cards | `EventCardsTest` | All event card behaviors |
| City Upgrade | `CityUpgradeTest` | City placement and victory points |
| Placement Rules | `PlacementRulesTest` | Placement validation |

### 3.4 Maintainability

**Definition:** The ease with which the system can be understood, corrected, and enhanced.

**Design Decisions Supporting Maintainability:**

1. **Clear Package Structure:**
   ```
   src/
   ├── model/           # Domain objects (Player, Card, Principality)
   ├── controller/      # Game flow and orchestration
   │   ├── actions/     # Action card effects
   │   ├── event/       # Event card effects
   │   ├── eventDieEvents/  # Event die effects
   │   ├── placement/   # Placement handlers
   │   └── settlement/  # Settlement/city expansion effects
   ├── io/              # Input/output abstractions
   ├── network/         # Network multiplayer (future)
   ├── util/            # Utilities and policies
   ├── view/            # Display logic
   └── tests/           # Unit tests
   ```
   - **Benefit:** Developers can quickly locate related functionality
   - **Example:** To modify an action card, look in `controller/actions/`

2. **Consistent Naming Conventions:**
   - Effects named after their card: `GoldsmithEffect`, `BrigandEffect`
   - Handlers named by pattern: `RoadPlacementHandler`, `CityPlacementHandler`
   - Interfaces prefixed with `I`: `ICardEffect`, `IInputService`
   - **Benefit:** Predictable names reduce search time

3. **Documentation and Comments:**
   - Interface methods documented with purpose
   - Complex logic explained with inline comments
   - Package-level documentation in `README.md` and `ARCHITECTURE.md`
   - **Example:**
     ```java
     /**
      * Central registry for mapping effect keys to effect factories.
      * Open for extension, closed for modification.
      */
     public final class EffectCatalog { ... }
     ```

4. **Single Source of Truth:**
   - Card data: `cards.json`
   - Game rules: `GameConfig`
   - Effect implementations: Individual effect classes
   - **Benefit:** No duplicate or conflicting information

5. **Minimal Dependencies:**
   - Model layer has no dependencies on controller or I/O
   - Effects depend only on `GameContext`
   - **Benefit:** Changes in one area don't cascade unnecessarily

6. **Automated Testing:**
   - 25+ unit tests cover core functionality
   - Tests serve as executable documentation
   - **Benefit:** Changes can be validated quickly, reducing fear of modification

**Maintainability Metrics:**

| Metric | Value | Interpretation |
|--------|-------|----------------|
| Average class size | ~100-200 lines | Manageable, focused classes |
| Average method size | ~10-30 lines | Easy to understand methods |
| Package cohesion | High | Related classes grouped together |
| Test coverage | Good | Core effects and rules tested |
| Documentation | Comprehensive | README, ARCHITECTURE, inline docs |

---

## 4. Design Patterns

### 4.1 Patterns Used

#### 4.1.1 Strategy Pattern

**Purpose:** Define a family of algorithms, encapsulate each one, and make them interchangeable.

**Implementation:**
- **Interface:** `ICardEffect`
- **Concrete Strategies:** `BrigandEffect`, `GoldsmithEffect`, `RelocationEffect`, etc.
- **Context:** `GameContext` provides environment for strategy execution

**Example:**
```java
public interface ICardEffect {
    void apply(GameContext ctx);
}

public class BrigandEffect implements ICardEffect {
    @Override
    public void apply(GameContext ctx) {
        // Brigand-specific logic
    }
}

public class GoldsmithEffect implements ICardEffect {
    @Override
    public void apply(GameContext ctx) {
        // Goldsmith-specific logic
    }
}
```

**Benefits:**
1. **New effects without modifying existing code:** Add new `ICardEffect` implementation
2. **Runtime composition:** Effects selected dynamically via `EffectCatalog`
3. **Testability:** Each effect can be tested independently
4. **Flexibility:** Same interface for diverse behaviors (action, event, passive effects)

**Why This Pattern:**
- Game has many cards with different behaviors
- Behaviors need to be added frequently (new expansions)
- Behaviors are complex enough to warrant separate classes
- Alternative (long switch statements) would violate OCP and be hard to maintain

#### 4.1.2 Registry Pattern

**Purpose:** Provide a centralized lookup for objects, enabling runtime composition and plugin architecture.

**Implementation:**
- **Registry:** `EffectCatalog`, `PlacementRegistry`
- **Registered Items:** Effect implementations, placement handlers

**Example - EffectCatalog:**
```java
public final class EffectCatalog {
    private final Map<String, Supplier<ICardEffect>> registry;
    
    public static EffectCatalog baseGame() {
        return builder()
            .register("brigand", BrigandEffect::new)
            .register("goldsmith", GoldsmithEffect::new)
            .register("relocation", RelocationEffect::new)
            // ... more effects
            .build();
    }
    
    public Optional<ICardEffect> get(String key) {
        Supplier<ICardEffect> factory = registry.get(key.toLowerCase());
        return factory != null ? Optional.of(factory.get()) : Optional.empty();
    }
}
```

**Example - PlacementRegistry:**
```java
public final class PlacementRegistry {
    private final List<PlacementHandler> handlers;
    
    public boolean tryPlace(BasicCard card, GameContext ctx) {
        for (var h : handlers) {
            if (h.canHandle(card)) {
                return h.place(card, ctx);
            }
        }
        return false;
    }
}
```

**Benefits:**
1. **Decoupling:** Clients don't need to know about concrete implementations
2. **Extensibility:** New items can be registered without modifying the registry
3. **Discoverability:** Central place to see all available implementations
4. **Consistency:** Uniform access mechanism

**Why This Pattern:**
- System needs to support plugins (new cards, new placement rules)
- Different configurations need different sets of effects (base game vs. expansions)
- Avoids hard-coding dependencies throughout the system

#### 4.1.3 Dependency Injection

**Purpose:** Provide dependencies from outside rather than creating them internally.

**Implementation:**
- Constructor injection for all major components
- No use of `new` for dependencies inside classes

**Example:**
```java
public class GameController {
    private final TurnManager turns;
    private final DeckManager decks;
    private final RuleValidator rules;
    private final IInputService in;
    private final IOutputService out;
    
    // Dependencies injected via constructor
    public GameController(TurnManager t, DeckManager d, RuleValidator r,
                          Randomizer rng, GameConfig cfg,
                          IInputService in, IOutputService out) {
        this.turns = t;
        this.decks = d;
        this.rules = r;
        this.in = in;
        this.out = out;
    }
}
```

**Benefits:**
1. **Testability:** Inject mocks/stubs for testing
2. **Flexibility:** Different configurations for different contexts
3. **Clarity:** All dependencies visible in constructor signature
4. **Reusability:** Same class can work with different implementations

**Why This Pattern:**
- Essential for testing (need to inject `MockIO`)
- Supports multiple configurations (console vs. network play)
- Makes dependencies explicit and manageable

#### 4.1.4 Template Method Pattern (Implicit)

**Purpose:** Define the skeleton of an algorithm, letting subclasses override specific steps.

**Implementation:**
- `TurnManager.playTurn()` defines turn sequence
- Individual methods can be customized

**Example:**
```java
public void playTurn(GameController game, Player current, Player opponent) {
    // Template structure
    TurnState ts = new TurnState();
    game.setCurrentTurnState(ts);
    
    // Step 1: Pre-roll phase
    preRoll.run(game, current, opponent);
    
    // Step 2: Roll dice
    int eventFace = eventMgr.rollFace();
    int prodRoll = ts.getProductionOverride().orElse(productionDie.roll(6));
    
    // Step 3: Resolve events and production
    if (eventFace == 1) {
        eventMgr.resolveFace(ctx, game.getDecks(), eventFace);
        game.produce(prodRoll, current, opponent);
    } else {
        game.produce(prodRoll, current, opponent);
        eventMgr.resolveFace(ctx, game.getDecks(), eventFace);
    }
    
    // Step 4: Action phase
    game.actionPhase(current, opponent);
    
    // Step 5: Replenish and exchange
    game.replenish(current);
    game.exchange(current);
}
```

**Benefits:**
1. **Consistency:** Turn flow always follows same structure
2. **Flexibility:** Individual steps can be customized
3. **Clarity:** High-level algorithm visible at a glance

**Why This Pattern:**
- Turn structure is well-defined but steps may vary
- Ensures consistent game flow
- Easier to understand and maintain than ad-hoc sequencing

#### 4.1.5 Factory Pattern

**Purpose:** Create objects without specifying exact class.

**Implementation:**
- `CardFactory` creates cards from JSON
- `EffectCatalog` acts as factory for effects

**Example:**
```java
public class CardFactory {
    public static List<Card> loadCardsFromJson(String path) {
        // Read JSON, parse, create appropriate card objects
        List<Card> cards = new ArrayList<>();
        
        for (JsonObject cardData : jsonArray) {
            String placement = cardData.get("placement").getAsString();
            
            if (placement.equals("Event")) {
                cards.add(new EventCard(name, ...));
            } else if (/* basic card conditions */) {
                cards.add(new BasicCard(name, cost, ...));
            } else {
                cards.add(new CenterCard(name, type, ...));
            }
        }
        
        return cards;
    }
}
```

**Benefits:**
1. **Encapsulation:** Card creation logic centralized
2. **Flexibility:** Can change creation logic without affecting clients
3. **Validation:** Factory can validate card data before creating objects

**Why This Pattern:**
- Cards created from external data (JSON)
- Need to instantiate different card types based on data
- Creation logic complex enough to warrant separate class

#### 4.1.6 Builder Pattern

**Purpose:** Construct complex objects step by step.

**Implementation:**
- `EffectCatalog.builder()` constructs catalog with fluent API

**Example:**
```java
public static class Builder {
    private final Map<String, Supplier<ICardEffect>> registry = new LinkedHashMap<>();
    
    public Builder register(String key, Supplier<ICardEffect> factory) {
        registry.put(key.toLowerCase(), factory);
        return this;
    }
    
    public Builder alias(String alias, String canonical) {
        Supplier<ICardEffect> factory = registry.get(canonical.toLowerCase());
        if (factory != null) registry.put(alias.toLowerCase(), factory);
        return this;
    }
    
    public EffectCatalog build() {
        return new EffectCatalog(registry);
    }
}

// Usage
EffectCatalog catalog = EffectCatalog.builder()
    .register("brigand", BrigandEffect::new)
    .register("goldsmith", GoldsmithEffect::new)
    .alias("merchant caravan", "caravan")
    .build();
```

**Benefits:**
1. **Readability:** Construction logic is clear and fluent
2. **Flexibility:** Can construct different configurations easily
3. **Immutability:** Built object can be immutable

**Why This Pattern:**
- `EffectCatalog` construction is complex (many effects, aliases)
- Fluent API improves readability
- Supports different catalogs for different eras

### 4.2 Patterns Intentionally NOT Used

#### 4.2.1 Singleton Pattern

**Why NOT Used:**
- **Testability Concerns:** Singletons create global state that's hard to mock
- **Flexibility Loss:** Can't have multiple configurations (e.g., different game instances)
- **Alternative:** Dependency Injection
  - `GameConfig`, `EffectCatalog` are created once and passed around
  - Tests can create multiple instances with different configurations
  - No hidden global dependencies

**Example of How We Avoid Singleton:**
```java
// NOT USED: Singleton pattern
public class GameConfig {
    private static GameConfig instance;
    public static GameConfig getInstance() { return instance; }
}

// USED INSTEAD: Dependency Injection
public class GameController {
    private final GameConfig cfg;
    
    public GameController(..., GameConfig cfg) {
        this.cfg = cfg;  // Injected, not singleton
    }
}
```

#### 4.2.2 Observer Pattern

**Why NOT Used:**
- **Simplicity:** Current event flow is synchronous and straightforward
- **No Need for Decoupling:** Event handlers invoked directly via `EventManager`
- **Alternative:** Direct method calls
  - `EventManager.resolveFace()` directly calls appropriate effect
  - `TurnManager` directly calls `GameController` methods
  - Simpler to understand and debug

**When It WOULD Be Useful:**
- If we needed to support multiple simultaneous listeners (e.g., logging, UI updates, sound effects)
- If events needed to be processed asynchronously
- Current design is simpler and sufficient for requirements

#### 4.2.3 Command Pattern

**Why NOT Used (Mostly):**
- **Simplicity:** Most actions are executed immediately
- **No Undo Needed:** Game doesn't require undo functionality
- **Alternative:** Direct effect application
  - `ICardEffect.apply()` executes immediately
  - No need to queue, defer, or undo commands

**Partial Use in Effect System:**
- `ICardEffect` is similar to Command pattern (encapsulates action)
- But we don't maintain command history or support undo

#### 4.2.4 Abstract Factory Pattern

**Why NOT Used:**
- **Unnecessary Complexity:** Single factory (`CardFactory`) is sufficient
- **No Product Families:** Cards are loaded from JSON, not created by type families
- **Alternative:** Simple Factory + Registry
  - `CardFactory` handles all card types
  - `EffectCatalog` provides effect lookup

**When It WOULD Be Useful:**
- If different eras required completely different card creation logic
- If we needed to create coordinated sets of related objects
- Current approach is simpler and meets requirements

#### 4.2.5 Visitor Pattern

**Why NOT Used:**
- **Complexity:** Visitor pattern adds significant complexity
- **Alternatives Are Clearer:** Strategy pattern (effects) is more straightforward
- **No Need for Open-Ended Operations:** Card operations are well-defined
  - Effects handle card execution
  - Placement handlers handle card placement
  - No need to add arbitrary operations to card hierarchy

**When It WOULD Be Useful:**
- If we needed to add many different operations to card hierarchy frequently
- If we wanted to keep operations separate from card classes
- Current design with effects is simpler and sufficient

---

## 5. Architecture for Future Extensibility

### 5.1 Supporting Different Eras

The architecture is explicitly designed to support multiple eras without modifying existing code.

**Mechanism: Configuration and Policy Overrides**

**Example: Age of Enlightenment Era**

1. **Create Era-Specific Configuration:**
   ```java
   public class EnlightenmentConfig extends GameConfig {
       @Override
       public int victoryPointsToWin() { 
           return 10;  // Different victory condition
       }
       
       @Override
       public int startHand() { 
           return 4;  // Start with 4 cards instead of 3
       }
       
       @Override
       public Map<String, List<Integer>> regionDice() {
           // Different region dice mapping
           return Map.of(...);
       }
   }
   ```

2. **Create Era-Specific Effects:**
   ```java
   public class ScienceAcademyEffect implements ICardEffect {
       @Override
       public void apply(GameContext ctx) {
           // New card behavior for Enlightenment era
       }
   }
   ```

3. **Create Era-Specific Catalog:**
   ```java
   public class EnlightenmentCatalog {
       public static EffectCatalog create() {
           return EffectCatalog.builder()
               // Include base effects
               .includeFrom(EffectCatalog.baseGame())
               // Add new effects
               .register("science academy", ScienceAcademyEffect::new)
               .register("university", UniversityEffect::new)
               .build();
       }
   }
   ```

4. **Bootstrap Game with Era Configuration:**
   ```java
   GameConfig config = new EnlightenmentConfig();
   EffectCatalog catalog = EnlightenmentCatalog.create();
   
   // Rest of initialization unchanged
   GameController controller = new GameController(..., config, ...);
   ```

**Result:** No modifications to base game code required. Era code is isolated in separate classes.

### 5.2 Supporting Expansions

**Mechanism: Additive Extensions**

**Example: "Wizards & Dragons" Expansion**

1. **Add Expansion Cards to JSON:**
   ```json
   {
     "name": "Dragon's Breath",
     "placement": "Action",
     "cost": "GGOO",
     "theme": "wizards",
     "effectKey": "dragon's breath"
   }
   ```

2. **Implement Expansion Effects:**
   ```java
   public class DragonBreathEffect implements ICardEffect {
       @Override
       public void apply(GameContext ctx) {
           // Destroy opponent's building
       }
   }
   ```

3. **Register in Expansion Catalog:**
   ```java
   public class WizardsCatalog {
       public static EffectCatalog create(EffectCatalog base) {
           return EffectCatalog.builder()
               .includeFrom(base)
               .register("dragon's breath", DragonBreathEffect::new)
               .register("wizard's tower", WizardTowerEffect::new)
               .build();
       }
   }
   ```

4. **Enable Expansion:**
   ```java
   EffectCatalog catalog = WizardsCatalog.create(EffectCatalog.baseGame());
   ```

**Result:** Expansion code isolated from base game. Can enable/disable expansions independently.

### 5.3 Supporting New Game Mechanics

**Mechanism: Policy Interfaces**

**Example: Adding "Seasons" Mechanic**

1. **Create Season Policy Interface:**
   ```java
   public interface SeasonPolicy {
       void applySeasonEffect(Player p, Season current);
       Season nextSeason(Season current);
   }
   ```

2. **Implement Season Policy:**
   ```java
   public class BasicSeasonPolicy implements SeasonPolicy {
       @Override
       public void applySeasonEffect(Player p, Season current) {
           switch (current) {
               case SPRING: // Bonus production
               case SUMMER: // Normal
               case AUTUMN: // Harvest bonus
               case WINTER: // Reduced production
           }
       }
       
       @Override
       public Season nextSeason(Season current) {
           // Rotate seasons
       }
   }
   ```

3. **Integrate with Turn Manager:**
   ```java
   public class TurnManager {
       private final SeasonPolicy seasonPolicy;
       
       public void playTurn(...) {
           // Apply season effect before production
           seasonPolicy.applySeasonEffect(current, currentSeason);
           
           // Rest of turn logic
       }
   }
   ```

**Result:** New mechanics can be added without modifying core game logic. Different eras can have different season policies.

---

## 6. Communication Flow and Interfaces

### 6.1 Turn Flow Sequence

```
Main.main()
    ↓
GameController.startGame()
    ↓
┌─────────────────────────────────────┐
│ For each turn:                      │
│ TurnManager.playTurn()              │
│   ├─ PreRollPhase.run()             │ ← Brigitta pre-roll effects
│   ├─ Roll event die                 │
│   ├─ Roll production die            │
│   ├─ Resolve event & production     │ ← Order depends on event (Brigand first)
│   │   ├─ EventManager.resolveFace() │
│   │   │   └─ ICardEffect.apply()    │
│   │   └─ DeckManager.produce()      │
│   ├─ GameController.actionPhase()   │
│   │   └─ Loop: play cards           │ ← Player plays action cards
│   ├─ GameController.replenish()     │ ← Draw from chosen stack
│   ├─ GameController.exchange()      │ ← Optional card exchange
│   └─ RuleValidator.hasWon()         │ ← Check victory
└─────────────────────────────────────┘
```

### 6.2 Card Play Flow

```
Player selects card from hand
    ↓
RuleValidator.canPlayBasicCard()  ← Check cost
    ↓
RuleValidator.payCost()  ← Deduct resources
    ↓
EffectCatalog.get(effectKey)  ← Lookup effect
    ↓
ICardEffect.apply(GameContext)  ← Execute effect
    ↓ (if placement card)
PlacementRegistry.tryPlace()  ← Find appropriate handler
    ↓
PlacementHandler.place()  ← Place on board
    ↓
Update Principality state  ← Update points, buildings, etc.
```

### 6.3 Key Interfaces

#### 6.3.1 Game Flow Interfaces

```java
// Main game controller
public interface IGameController {
    void startGame();
    void actionPhase(Player current, Player opponent);
    void replenish(Player p);
    void exchange(Player p);
}

// Turn management
public class TurnManager {
    public void playTurn(GameController game, Player current, Player opponent);
}

// Event handling
public interface IEventHandler {
    void resolveFace(GameContext ctx, DeckManager decks, int face);
}
```

#### 6.3.2 Card Effect Interface

```java
// Core effect interface
public interface ICardEffect {
    void apply(GameContext ctx);
    default String description() { return getClass().getSimpleName(); }
}

// Context for effect execution
public class GameContext {
    public Player currentPlayer();
    public Player opponent();
    public IInputService input();
    public IOutputService output();
    public DeckManager decks();
    public RuleValidator rules();
    // ... other context methods
}
```

#### 6.3.3 I/O Interfaces

```java
// Input operations
public interface IInputService {
    String readLine();
    int readInt();
    boolean readBoolean();
}

// Output operations
public interface IOutputService {
    void print(String message);
    void println(String message);
}

// Combined player I/O
public interface IPlayerIO {
    IInputService in();
    IOutputService out();
}
```

#### 6.3.4 Placement Interface

```java
public interface PlacementHandler {
    boolean canHandle(BasicCard card);
    boolean place(BasicCard card, GameContext ctx);
}
```

#### 6.3.5 Validation Interface

```java
public interface IRuleValidator {
    boolean hasWon(Player p);
    boolean canPlayBasicCard(Player p, BasicCard card);
    void payCost(Player p, BasicCard card);
    String explainCostFailure(Player p, BasicCard card);
}
```

---

## 7. Developer Guide: Where to Find and Add Functionality

### 7.1 Adding a New Action Card

**Steps:**

1. **Create Effect Class** (in `src/controller/actions/`):
   ```java
   package src.controller.actions;
   
   import src.controller.ICardEffect;
   import src.controller.GameContext;
   
   public class MyNewCardEffect implements ICardEffect {
       @Override
       public void apply(GameContext ctx) {
           // Implement card behavior
           ctx.output().println("My New Card activated!");
           // ... card logic
       }
   }
   ```

2. **Register in EffectCatalog** (in `src/controller/EffectCatalog.java`):
   ```java
   public static EffectCatalog baseGame() {
       return builder()
           // ... existing effects
           .register("my new card", MyNewCardEffect::new)
           .build();
   }
   ```

3. **Add to cards.json**:
   ```json
   {
     "name": "My New Card",
     "placement": "Action",
     "cost": "BGL",
     "theme": "basic",
     "effectKey": "my new card",
     "number": 2
   }
   ```

4. **Create Test** (in `src/tests/`):
   ```java
   public class MyNewCardTest {
       public static void main(String[] args) {
           // Test card behavior
       }
   }
   ```

### 7.2 Adding a New Event

**Steps:**

1. **Create Effect Class** (in `src/controller/event/`):
   ```java
   package src.controller.event;
   
   import src.controller.ICardEffect;
   import src.controller.GameContext;
   
   public class MyEventEffect implements ICardEffect {
       @Override
       public void apply(GameContext ctx) {
           // Implement event behavior
       }
   }
   ```

2. **Register with "event:" prefix**:
   ```java
   .register("event:my event", MyEventEffect::new)
   ```

3. **Add to cards.json**:
   ```json
   {
     "name": "My Event",
     "placement": "Event",
     "theme": "basic",
     "effectKey": "event:my event"
   }
   ```

### 7.3 Adding a New Placement Type

**Steps:**

1. **Create Handler** (in `src/controller/placement/`):
   ```java
   package src.controller.placement;
   
   import src.model.BasicCard;
   import src.controller.GameContext;
   
   public class MyPlacementHandler implements PlacementHandler {
       @Override
       public boolean canHandle(BasicCard card) {
           return card.getName().contains("My Structure");
       }
       
       @Override
       public boolean place(BasicCard card, GameContext ctx) {
           // Implement placement logic
           return true;
       }
   }
   ```

2. **Register Handler** (in `GameController` constructor):
   ```java
   this.placementRegistry = new PlacementRegistry(
       Arrays.asList(
           // ... existing handlers
           new MyPlacementHandler()
       )
   );
   ```

### 7.4 Modifying Game Rules

**Configuration Changes** (in `src/util/GameConfig.java`):
```java
public class GameConfig {
    public int victoryPointsToWin() { 
        return 10;  // Change from 7 to 10
    }
}
```

**Complex Rule Changes:**
- Modify `RuleValidator` for validation logic
- Modify `TurnManager` for turn flow changes
- Add new policy interface for major variations

### 7.5 Adding a New Resource Type

**Steps:**

1. **Add to Resource enum** (in `src/model/Resource.java`):
   ```java
   public enum Resource {
       LUMBER, BRICK, WOOL, GRAIN, ORE, GOLD, MAGIC  // Added MAGIC
   }
   ```

2. **Update CostParser** if needed

3. **Update affected effects** that reference resources explicitly

4. **Update RegionTile** if new resource comes from regions

### 7.6 Adding Network Support

**Already Supported:**
- Package `src/network/` contains infrastructure
- `SocketInput` and `SocketOutput` provide network I/O
- `Server`, `ClientConnection`, `ServerHandler` provide server framework

**To Enable:**
1. Implement complete networking logic in `network/` package
2. Use `SocketInput`/`SocketOutput` for player I/O
3. No changes needed to game logic

---

## 8. Summary and Conclusion

### 8.1 Design Strengths

1. **Highly Modular Architecture:**
   - Clear separation of concerns across packages
   - Model, controller, view, I/O cleanly separated
   - Each component has well-defined responsibility

2. **Extensibility First:**
   - Effect system allows new cards without code changes
   - Placement system supports new structures via plugins
   - Policy interfaces enable rule variations
   - Configuration centralized for easy customization

3. **Strong SOLID Adherence:**
   - Single Responsibility: Each class has one reason to change
   - Open/Closed: Open for extension via interfaces and registries
   - Liskov Substitution: Polymorphism works correctly throughout
   - Interface Segregation: Minimal, focused interfaces
   - Dependency Inversion: High-level code depends on abstractions

4. **Testability:**
   - Dependency injection throughout
   - MockIO for test automation
   - Small, focused classes easy to test in isolation
   - 25+ unit tests covering core functionality

5. **Maintainability:**
   - Clear package structure
   - Consistent naming conventions
   - Comprehensive documentation
   - Low coupling, high cohesion

### 8.2 How Quality Attributes Are Achieved

| Quality Attribute | Key Mechanisms | Evidence |
|-------------------|----------------|-----------|
| **Modifiability** | Centralized config, Effect registry, Data-driven cards | Change victory points: 1 file. Add new card: 2 files. |
| **Extensibility** | Strategy pattern, Registry pattern, Policy interfaces | New eras/expansions: isolated code, no base modifications |
| **Testability** | Dependency injection, MockIO, Small classes | 25+ tests, all using MockIO, effects testable independently |
| **Maintainability** | Package structure, Clear naming, Documentation | Predictable locations, self-documenting code, comprehensive docs |

### 8.3 Design Trade-offs

**Complexity vs. Flexibility:**
- Trade-off: More abstractions (interfaces, registries) add initial complexity
- Benefit: Massive flexibility for future extensions and testing
- Decision: Justified by requirements for multiple eras and expansions

**Performance vs. Flexibility:**
- Trade-off: Registry lookups and indirect calls vs. direct method calls
- Impact: Negligible for turn-based card game
- Benefit: Plugin architecture enables extensibility
- Decision: Flexibility more important than microsecond performance

**Code Size vs. Maintainability:**
- Trade-off: More classes (one per effect) vs. fewer larger classes
- Benefit: Each class is simple and testable
- Decision: Many small classes better than few large classes

### 8.4 Future-Proofing

The architecture is explicitly designed to support:

1. **Different Eras:**
   - Extend `GameConfig` for era-specific rules
   - Provide era-specific `EffectCatalog`
   - No changes to core game logic

2. **Expansions:**
   - Add new effects via `ICardEffect` implementations
   - Add new cards to `cards.json`
   - Register in expansion-specific catalog

3. **New Game Mechanics:**
   - Add policy interfaces for new variation points
   - Implement policies for different eras
   - Inject policies into managers

4. **Different Interfaces:**
   - GUI: Implement `IInputService`/`IOutputService` for GUI
   - Mobile: Same approach
   - Web: Same approach
   - No changes to game logic required

5. **AI Players:**
   - Implement `AIInputService` with strategy logic
   - Inject for AI players
   - Game logic unchanged

### 8.5 Recommendations for Developers

1. **When Adding New Cards:**
   - Create effect class in appropriate package (`actions`, `event`, `settlement`)
   - Register in `EffectCatalog`
   - Add to `cards.json`
   - Write test

2. **When Modifying Rules:**
   - Check if it's configuration: modify `GameConfig`
   - Check if it's validation: modify `RuleValidator`
   - Check if it's flow: modify `TurnManager`
   - Create policy interface if it's a variation point

3. **When Testing:**
   - Use `MockIO` for all tests
   - Test effects in isolation using `GameContext`
   - Create minimal test setups
   - Follow existing test patterns

4. **When Documenting:**
   - Update `ARCHITECTURE.md` for structural changes
   - Update this document for design changes
   - Add inline comments for complex logic
   - Keep `README.md` current with build instructions

### 8.6 Conclusion

The refactored architecture successfully transforms a monolithic implementation into a modular, extensible, and maintainable system. It adheres strongly to SOLID principles, maintains good Booch metrics (low coupling, high cohesion), and explicitly addresses key quality attributes. The design patterns employed (Strategy, Registry, Dependency Injection, Factory, Builder) serve clear purposes and improve the overall design quality.

Most importantly, the architecture is prepared for future growth: new eras, expansions, and game mechanics can be added with minimal or no changes to existing code. The separation of concerns, interface-based design, and policy-driven variation points provide the flexibility needed for a game system that will evolve over time.

The architecture demonstrates that good software design is not about clever tricks or complex patterns, but about clear structure, appropriate abstractions, and consistent application of proven principles. Every design decision can be traced back to a specific quality attribute or principle, making the architecture both understandable and maintainable for future developers.

---

## Appendix A: Package Responsibilities

| Package | Responsibility | Key Classes |
|---------|----------------|-------------|
| `model` | Domain objects and state | `Player`, `Card`, `Principality`, `Resource` |
| `controller` | Game flow orchestration | `GameController`, `TurnManager`, `EventManager` |
| `controller.actions` | Action card effects | `GoldsmithEffect`, `RelocationEffect`, `Scout` |
| `controller.event` | Event card effects | `YuleEffect`, `InventionEffect`, `FeudEffect` |
| `controller.eventDieEvents` | Event die effects | `BrigandEffect`, `TradeEffect`, `CelebrationEffect` |
| `controller.placement` | Card placement logic | `PlacementRegistry`, various handlers |
| `controller.settlement` | Settlement/city expansions | `AbbeyEffect`, `MarketplaceEffect`, `TollBridgeEffect` |
| `io` | Input/output abstractions | `IInputService`, `ConsoleInput`, `MockIO` |
| `network` | Network multiplayer | `Server`, `ClientConnection`, `OnlinePlayer` |
| `util` | Utilities and policies | `GameConfig`, `Dice`, `Randomizer`, policy interfaces |
| `view` | Display logic | `BoardPrinter` |
| `tests` | Unit tests | 25+ test classes |

## Appendix B: Key Interfaces

| Interface | Purpose | Implementations |
|-----------|---------|----------------|
| `ICardEffect` | Card behavior | 30+ effect classes |
| `PlacementHandler` | Card placement | 8 placement handlers |
| `IInputService` | Input operations | `ConsoleInput`, `SocketInput`, `MockIO` |
| `IOutputService` | Output operations | `ConsoleOutput`, `SocketOutput`, `MockIO` |
| `IPlayerIO` | Combined I/O | Anonymous implementations |
| `VictoryPolicy` | Victory conditions | `BasePolicies` (future: era-specific) |
| `ProductionPolicy` | Production rules | `BasePolicies` (future: era-specific) |
| `PlacementPolicy` | Placement rules | `BasePolicies` (future: era-specific) |
| `TradePolicy` | Trade rules | `BasePolicies` (future: era-specific) |

## Appendix C: Extension Points

| Extension Point | Mechanism | Example |
|----------------|-----------|---------|
| New card effects | Implement `ICardEffect` | `DragonBreathEffect` |
| New placement types | Implement `PlacementHandler` | `ShipPlacementHandler` |
| New eras | Extend `GameConfig` | `EnlightenmentConfig` |
| New game mechanics | Create policy interface | `SeasonPolicy` |
| New I/O methods | Implement `IInputService`/`IOutputService` | `GUIInputService` |
| New resources | Add to `Resource` enum | `MAGIC` resource |
| New victory conditions | Override `GameConfig.victoryPointsToWin()` | Era-specific rules |

---

**Document Version:** 1.0  
**Last Updated:** 2025-10-21  
**Authors:** Development Team  
**Audience:** Developers, Architects, Maintainers
