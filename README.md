# HomeExam - Rivals for Catan Implementation

### Quick Structure Reference

```
src/
├── Main.java                       # Application entry point
│
├── model/                          # Domain models (Player, Card, Deck, etc.)
├── controller/                     # Game logic and control flow
│   ├── actions/                    # Action card effects
│   ├── event/                      # Event card effects
│   ├── eventDieEvents/             # Event die outcomes
│   ├── phases/                     # Game phase handlers
│   ├── placement/                  # Placement logic and validation
│   ├── settlement/                 # Settlement-specific effects
│   │   ├── buildings/              # Building effects
│   │   └── units/                  # Unit effects
│   └── interfaces/                 # Controller interfaces
│
├── view/                           # Display and rendering (BoardPrinter)
├── io/                             # I/O abstractions (Console/Socket/Mock)
├── network/                        # Network multiplayer support
├── util/                           # Utilities (Dice, Config, Logger, Policies)
└── tests/                          # Unit tests
```

> **Note:** See [FOLDER_STRUCTURE.md](FOLDER_STRUCTURE.md) for the complete detailed structure with all files.

## Build and Run

```bash
# from /home/simon/HomeExam-refactored-updated/src
rm -rf ../bin
mkdir -p ../bin

# compile with the gson.jar one level up
javac -d ../bin -cp "../gson.jar" $(find . -name "*.java")

# Terminal A (host)
java -cp "../bin:../gson.jar" src.network.Server

# Terminal B (client)
java -cp "../bin:../gson.jar" src.network.Server online

```
