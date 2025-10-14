# HomeExam

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
│       ├── HarvestEffect.java        // Implements harvest behavior
│       └── EventCardEffect.java      // Base for event-card-driven effects
│
├── network/                        // The network/ package isolates everything related to online gameplay, socket communication, and message handling.
    |                               // It acts as a bridge between the game controller (which runs the game logic) and remote players (connected via TCP/IP).
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
