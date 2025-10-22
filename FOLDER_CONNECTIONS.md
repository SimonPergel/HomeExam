# Folder Connection Overview

This document illustrates how the different packages/folders are connected and interact with each other.

## Package Dependencies and Relationships

```mermaid
graph LR
    subgraph "Entry Point"
        Main[Main.java]
    end

    subgraph "Presentation Layer"
        View[view/]
        IO[io/]
    end

    subgraph "Business Logic Layer"
        Controller[controller/]
        Actions[controller/actions/]
        Events[controller/event/]
        EventDie[controller/eventDieEvents/]
        Phases[controller/phases/]
        Placement[controller/placement/]
        Settlement[controller/settlement/]
    end

    subgraph "Domain Layer"
        Model[model/]
    end

    subgraph "Infrastructure Layer"
        Network[network/]
        Util[util/]
        Policy[util/policy/]
    end

    subgraph "Testing Layer"
        Tests[tests/]
    end

    %% Main dependencies
    Main --> Controller
    Main --> IO
    Main --> View

    %% Controller dependencies
    Controller --> Model
    Controller --> IO
    Controller --> Util
    Controller --> Actions
    Controller --> Events
    Controller --> EventDie
    Controller --> Phases
    Controller --> Placement
    Controller --> Settlement

    %% Sub-controller dependencies
    Actions --> Model
    Events --> Model
    EventDie --> Model
    Phases --> Model
    Placement --> Model
    Settlement --> Model

    %% View dependencies
    View --> Model
    View --> IO

    %% IO dependencies
    IO --> Network

    %% Network dependencies
    Network --> Model
    Network --> IO

    %% Util dependencies
    Policy --> Model

    %% Test dependencies
    Tests --> Controller
    Tests --> Model
    Tests --> IO

    style Main fill:#ffcccc
    style Controller fill:#ccffcc
    style Model fill:#ccccff
    style IO fill:#ffffcc
    style Network fill:#ffccff
```

## Detailed Package Interaction Diagram

```mermaid
graph TB
    subgraph "Client Entry"
        Main["Main.java<br/>Application Entry Point"]
    end

    subgraph "User Interface"
        View["view/<br/>BoardPrinter"]
        IOLayer["io/<br/>Console/Socket/Mock IO"]
        IOInterfaces["io/interfaces/<br/>IPlayerIO, IInput/OutputService"]
    end

    subgraph "Core Controller"
        GameController["GameController<br/>Main game coordinator"]
        TurnManager["TurnManager<br/>Turn sequencing"]
        DeckManager["DeckManager<br/>Card management"]
        EventManager["EventManager<br/>Event coordination"]
        CardFactory["CardFactory<br/>Card creation"]
        RuleValidator["RuleValidator<br/>Rule enforcement"]
        EffectRegistry["EffectRegistry<br/>Effect registration"]
        SetupService["SetupService<br/>Game initialization"]
        GameContext["GameContext<br/>Shared state"]
    end

    subgraph "Effect Implementations"
        Actions["actions/<br/>Card effects:<br/>Goldsmith, Merchant, etc."]
        Events["event/<br/>Event effects:<br/>Feud, Invention, etc."]
        EventDie["eventDieEvents/<br/>Die outcomes:<br/>Brigand, Trade, etc."]
        Settlement["settlement/<br/>Buildings & Units"]
    end

    subgraph "Game Flow"
        Phases["phases/<br/>PreRollPhase"]
        Placement["placement/<br/>Placement handlers"]
    end

    subgraph "Domain Models"
        Card["Card hierarchy:<br/>Basic, Event, Center"]
        Player["Player & Principality"]
        Deck["Deck"]
        Resources["Resources & Points"]
    end

    subgraph "Network Layer"
        Server["Server & ServerHandler"]
        ClientConn["ClientConnection"]
        OnlinePlayer["OnlinePlayer"]
        NetworkService["NetworkService"]
    end

    subgraph "Utilities"
        Dice["Dice & Randomizer"]
        Config["GameConfig"]
        Logger["Logger"]
        CostParser["CostParser"]
        Policies["policy/<br/>Game policies"]
    end

    %% Main connections
    Main --> GameController
    Main --> IOLayer
    Main --> View

    %% Controller orchestration
    GameController --> TurnManager
    GameController --> DeckManager
    GameController --> EventManager
    GameController --> RuleValidator
    GameController --> SetupService
    GameController --> GameContext

    %% Factory pattern
    CardFactory --> Card
    CardFactory --> EffectRegistry

    %% Effect execution flow
    GameController --> Actions
    GameController --> Events
    EventManager --> EventDie
    GameController --> Phases
    GameController --> Placement
    GameController --> Settlement

    %% Effects interact with models
    Actions --> Player
    Events --> Player
    EventDie --> Player
    Settlement --> Player

    %% Placement flow
    Placement --> Player
    Placement --> RuleValidator

    %% Model interactions
    Player --> Card
    Player --> Deck
    Player --> Resources

    %% View rendering
    View --> Player
    View --> IOLayer

    %% IO abstraction
    IOLayer --> IOInterfaces
    GameController --> IOInterfaces

    %% Network multiplayer
    Server --> ClientConn
    ClientConn --> OnlinePlayer
    OnlinePlayer --> Player
    OnlinePlayer --> IOLayer
    NetworkService --> Server

    %% Utilities used throughout
    GameController --> Dice
    GameController --> Config
    GameController --> Logger
    DeckManager --> CostParser
    RuleValidator --> Policies

    style Main fill:#ff9999
    style GameController fill:#99ff99
    style Card fill:#9999ff
    style IOLayer fill:#ffff99
    style Server fill:#ff99ff
```

## Layer Architecture

```mermaid
graph TD
    subgraph "Layer 1: Presentation"
        L1A[Main.java]
        L1B[view/BoardPrinter]
    end

    subgraph "Layer 2: Application Services"
        L2A[controller/GameController]
        L2B[controller/TurnManager]
        L2C[controller/DeckManager]
        L2D[controller/EventManager]
        L2E[controller/SetupService]
    end

    subgraph "Layer 3: Business Logic"
        L3A[controller/actions/]
        L3B[controller/event/]
        L3C[controller/eventDieEvents/]
        L3D[controller/phases/]
        L3E[controller/placement/]
        L3F[controller/settlement/]
        L3G[controller/RuleValidator]
    end

    subgraph "Layer 4: Domain Model"
        L4A[model/Card]
        L4B[model/Player]
        L4C[model/Principality]
        L4D[model/Deck]
        L4E[model/Resource]
    end

    subgraph "Layer 5: Infrastructure"
        L5A[io/]
        L5B[network/]
        L5C[util/]
    end

    L1A --> L2A
    L1A --> L5A
    L1B --> L4B
    L1B --> L5A

    L2A --> L2B
    L2A --> L2C
    L2A --> L2D
    L2A --> L2E
    L2A --> L3G
    L2A --> L5C

    L2B --> L3A
    L2B --> L3D
    L2C --> L4D
    L2D --> L3B
    L2D --> L3C

    L3A --> L4A
    L3A --> L4B
    L3B --> L4B
    L3C --> L4B
    L3D --> L4B
    L3E --> L4B
    L3E --> L4C
    L3F --> L4B
    L3G --> L4B

    L5A --> L5B
    L5B --> L4B
```

## Communication Flow

```mermaid
sequenceDiagram
    participant Main
    participant GameController
    participant TurnManager
    participant EventManager
    participant Player
    participant IO
    participant View

    Main->>GameController: Initialize game
    GameController->>Player: Create players
    GameController->>IO: Setup I/O channels
    
    loop Each Turn
        GameController->>TurnManager: Start turn
        TurnManager->>Player: Get current player
        TurnManager->>EventManager: Roll event die
        EventManager->>Player: Apply event effects
        TurnManager->>Player: Execute turn phases
        Player->>IO: Request player input
        IO->>Player: Return player choice
        Player->>GameController: Execute action
        GameController->>View: Update display
        View->>IO: Render board state
    end
    
    GameController->>Player: Check victory condition
    GameController->>View: Display winner
```

## Key Relationships

### 1. **Controller → Model**
   - Controllers orchestrate game logic
   - Models maintain game state
   - One-way dependency: Controller uses Model

### 2. **Controller → IO**
   - Controllers use IO for player interaction
   - IO abstracts console vs network
   - Dependency Injection via interfaces

### 3. **Network → IO**
   - Network implements IO interfaces
   - Enables remote gameplay
   - Same interface as console

### 4. **View → Model**
   - View reads model state
   - View uses IO for output
   - No direct model modification

### 5. **Effects → Model**
   - All effects (actions, events, settlements) modify Player/Principality
   - Registered in EffectRegistry
   - Invoked by GameController

### 6. **Util → (Used by all)**
   - Provides cross-cutting concerns
   - Dice, Config, Logger, Policies
   - No dependencies on other packages
