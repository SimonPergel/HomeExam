# HomeExam - Rivals for Catan Implementation

## Documentation

This repository contains a refactored implementation of Rivals for Catan following SOLID principles and best practices.

**Key Documentation:**
- **[DESIGN_REPORT.md](DESIGN_REPORT.md)** - Comprehensive design analysis including SOLID principles, Booch metrics, quality attributes, and design patterns
- **[ARCHITECTURE.md](ARCHITECTURE.md)** - Detailed architecture overview and technical implementation details
- **[TEST_SUMMARY.md](TEST_SUMMARY.md)** - Unit test coverage and testing approach

**Project Structure & Diagrams:**
- **[FOLDER_STRUCTURE.md](FOLDER_STRUCTURE.md)** - Complete folder and class structure with detailed diagrams
- **[FOLDER_CONNECTIONS.md](FOLDER_CONNECTIONS.md)** - Package dependencies and relationships overview
- **[GAME_FLOW.md](GAME_FLOW.md)** - Game flow, turn flow, and control flow diagrams

## Quick Start

See the build instructions below to compile and run the game.

## Project Structure Overview

For a comprehensive view of the project structure, see the detailed diagrams in:
- **[Folder Structure](FOLDER_STRUCTURE.md)** - Complete class hierarchy
- **[Folder Connections](FOLDER_CONNECTIONS.md)** - How packages interact
- **[Game Flow](GAME_FLOW.md)** - Game execution flow

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
cd /home/runner/work/HomeExam/HomeExam/src
rm -rf ../bin
mkdir -p ../bin
javac -cp ../gson.jar -d ../bin $(find . -name "*.java")
java -cp ../bin:../gson.jar Main

```

## What's Left for the Intro Game


**1. Action cards (Base set)**

    - You already have the plumbing (BasicCard.effect, EffectRegistry, action-phase loop).

    - Next: implement the base-game action cards only (no expansion/era cards).
    You’ve got Merchant Caravan and Goldsmith done; I can add the rest you need from your cards.json (base-only) the same way.

**2. Costs & prerequisites**

    - You added cost parsing—great. We’ll keep it strictly for base cards.

    - If any base card has a prerequisite (e.g., needs settlement/city), we’ll enforce that via RuleValidator (still base-only).

**3. Production mapping (base)**

    - Replace the simplified production with base-game region mapping: production die → give resources from the player’s regions.

    - Minimal principality model (just enough for base): settlements/cities row with their two adjacent region slots is enough to handle Storehouse/Weaver’s Shop/Iron Foundry later (still base set).

**4. Events (base)**

    - Already covered: Brigand, Trade, Celebration, Plentiful Harvest, Invention (hooked).

    - Keep event handling aligned with base-game rules.

**5. Networking (optional)**

    - Not required to be feature-complete; we keep the IO/network boundaries so you can plug it in later if you want, but we won’t add era/expansion logic.

**Extensibility (no extra content implemented)**

    - We keep EffectRegistry, CardFactory, RuleValidator, and GameConfig design so future packs can register—but we won’t add any extra cards/eras now.