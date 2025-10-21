# HomeExam - Rivals for Catan Implementation

## Documentation

This repository contains a refactored implementation of Rivals for Catan following SOLID principles and best practices.

**Key Documentation:**
- **[DESIGN_REPORT.md](DESIGN_REPORT.md)** - Comprehensive design analysis including SOLID principles, Booch metrics, quality attributes, and design patterns
- **[ARCHITECTURE.md](ARCHITECTURE.md)** - Detailed architecture overview and technical implementation details
- **[TEST_SUMMARY.md](TEST_SUMMARY.md)** - Unit test coverage and testing approach

## Quick Start

See the build instructions below to compile and run the game.

__Folder/class structure__

```
src/
├── model/
│   ├── Card.java
│   ├── Deck.java
│   ├── Player.java
│   ├── Principality.java
│   ├── Resource.java
│   ├── EventCard.java
│   ├── BasicCard.java
│   ├── CenterCard.java
│   └── VictoryPointTracker.java
│
├── controller/
│   ├── GameController.java
│   ├── TurnManager.java
│   ├── DeckManager.java
│   ├── EventManager.java
│   ├── CardFactory.java
│   ├── RuleValidator.java
│   ├── interfaces/
│   │   ├── IGameController.java
│   │   ├── IDeckProvider.java
│   │   ├── IEventHandler.java
│   │   └── IRuleValidator.java
│   │
│   └── actions/
│       ├── ICardEffect.java          // Interface for all effects
│       ├── BrigandEffect.java        // Implements Brigand attack behavior
│       ├── TradeEffect.java          // Implements trade behavior
│       ├── CelebrationEffect.java    // Implements celebration behavior
│       ├── HarvestEffect.java        // Implements harvest behavior, each player reccive 1 resorce of his choice
│       |── EventCardEffect.java      // Base for event-card-driven effects
|       ├── TradeEffect.java
|       ├── RoadBuildingEffect.java
│
├── network/                        // The network/ package isolates everything related to online gameplay, socket communication, and message handling.
|    |                               // It acts as a bridge between the game controller (which runs the game logic) and remote players (connected via TCP/IP).
│   ├── Server.java                 // Bootstraps online sessions, the main entry point for hosting a game session
│   ├── ServerHandler.java          // Keeps network event handling separate from the game flow, Manages multiple connected players (threads or sessions)
│   ├── ClientConnection.java       // one connection between the server and a single remote player, One object per connected player. Keeps connection logic isolated and testable
│   ├── OnlinePlayer.java           // Integrates networking with the Player model, without changing core game logic.
│   ├── NetworkService.java         // Simplifies client connection logic and ensures reusability, 
│   └── interfaces/
│       ├── INetworkHandler.java
│       ├── IConnection.java
│       └── IMessageProtocol.java
│
├── io/                             // Handles I/O abstractions (console, network or mock)
│   ├── interfaces/
│   │   ├── IPlayerIO.java          // unified channel for sending/receiving game messages
│   │   ├── IInputService.java      // Abstracts input collection (e.g., read a command, card choice, or dice roll).
│   │   └── IOutputService.java     // Abstracts message sending (e.g., display board, prompt user).
│   │
│   ├── ConsoleInput.java           // Used for local play or debugging. Reads from keyboard, prints to terminal.
│   ├── ConsoleOutput.java          //                              --||--
│   ├── SocketInput.java            // Handles network I/O using sockets.
│   ├── SocketOutput.java
│   └── MockIO.java                 // For JUnit testing
│
├── util/
│   ├── Dice.java
│   ├── Logger.java
│   ├── Randomizer.java
│   └── GameConfig.java
```

__Rebuild and Run__

```
cd ~/HomeExam-refactored-updated/src
rm -rf ../bin
mkdir -p ../bin
javac -cp ../gson.jar -d ../bin $(find . -name "*.java")
java -cp ../bin:../gson.jar src.Main

```
## Whats left fot he Intro game


__1. Action cards (Base set)__

    - You already have the plumbing (BasicCard.effect, EffectRegistry, action-phase loop).

    - Next: implement the base-game action cards only (no expansion/era cards).
    You’ve got Merchant Caravan and Goldsmith done; I can add the rest you need from your cards.json (base-only) the same way.

__2. Costs & prerequisites__

    - You added cost parsing—great. We’ll keep it strictly for base cards.

    - If any base card has a prerequisite (e.g., needs settlement/city), we’ll enforce that via RuleValidator (still base-only).

__3. Production mapping (base)__

    - Replace the simplified production with base-game region mapping: production die → give resources from the player’s regions.

    - Minimal principality model (just enough for base): settlements/cities row with their two adjacent region slots is enough to handle Storehouse/Weaver’s Shop/Iron Foundry later (still base set).

__4. Events (base)__

    - Already covered: Brigand, Trade, Celebration, Plentiful Harvest, Invention (hooked).

    - Keep event handling aligned with base-game rules.

__5. Networking (optional)__

    - Not required to be feature-complete; we keep the IO/network boundaries so you can plug it in later if you want, but we won’t add era/expansion logic.

__Extensibility (no extra content implemented)__

    - We keep EffectRegistry, CardFactory, RuleValidator, and GameConfig design so future packs can register—but we won’t add any extra cards/eras now.