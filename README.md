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
├── network/
│   ├── Server.java
│   ├── ServerHandler.java
│   ├── ClientConnection.java
│   ├── OnlinePlayer.java
│   ├── NetworkService.java
│   └── interfaces/
│       ├── INetworkHandler.java
│       ├── IConnection.java
│       └── IMessageProtocol.java
│
├── io/
│   ├── interfaces/
│   │   ├── IPlayerIO.java          // Generic input/output abstraction
│   │   ├── IInputService.java
│   │   └── IOutputService.java
│   │
│   ├── ConsoleInput.java
│   ├── ConsoleOutput.java
│   ├── SocketInput.java
│   ├── SocketOutput.java
│   └── MockIO.java                 // For JUnit testing
│
├── util/
│   ├── Dice.java
│   ├── Logger.java
│   ├── Randomizer.java
│   └── GameConfig.java
```
