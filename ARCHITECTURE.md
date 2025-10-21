# Architecture overview

This codebase refactors the legacy single-file implementation into layered modules designed for modifiability, extensibility, and testability.

- Controller layer: game flow and rules orchestration
  - `TurnManager`: turn sequence (pre-roll, event die vs. production order, action, replenish, exchange, win check)
  - `GameController`: ties I/O, rules, decks, and placement; number-based action UI to match legacy console
  - `EventManager`: resolves event-die faces 1-4 and event cards (5-6)
  - `RuleValidator`: costs, dynamic preconditions (e.g., Goldsmith and Merchant Caravan), victory policy
  - `EffectCatalog`: registry mapping string keys to effect strategies; open for extension
  - Placement subpackage: `PlacementRegistry` plus handlers for Road/Settlement/City/Region
  - Phases: `PreRollPhase` to support effects like Brigitta before dice

- Model layer: immutable `Card` base, `BasicCard` with effect strategy and cost, `CenterCard` categories, `EventCard`, `Deck<T>`, `Principality` board and `RegionTile`, `Points`, `Player` (resources, hand, board)

- Decks and setup
  - `CardFactory`: parses `cards.json` (number is numeric), filters theme contains "basic", expands duplicates by `number`; partitions into center piles (regions/settlements/cities/roads), basic pool, and event cards; positions "Yule" 4th from bottom
  - `DeckManager`: holds four basic stacks (round-robin distribution), center piles, event stack; starting hand draw from chosen stack; production fills per-region storage 0..3; replenish and optional exchange flows
  - `SetupService`: seeds the intro grid matching the legacy layout

- Effects
  - Event die: Brigand, Trade, Celebration, Plentiful Harvest (HarvestEffect), and event card effects (Feud, Fraternal Feuds, Invention, Trade Ships Race, Traveling Merchant, Year of Plenty, Yule)
  - Actions: Goldsmith, Merchant, Merchant Caravan, Road Building, Relocation, Brigitta, Abbey, common heroes and trade ships

- I/O abstractions
  - `IInputService`/`IOutputService` with concrete `ConsoleInput`/`ConsoleOutput` and socket variants (`SocketInput`/`SocketOutput`) for future two-terminal play. One player per terminal remains the target UX.

- Policies and config
  - `GameConfig` centralizes counts (49 center, 36 basic, 9 events), Yule offset, VP to win (7), and region dice assignment; dedicated policy interfaces under `util/policy` to facilitate era/expansion overrides

- Networking (future)
  - Stubs under `src/network` outline `Server`, `ClientConnection`, `ServerHandler`, `OnlinePlayer`. The socket I/O adapters can be composed here to support (1 local + 1 remote) or (2 remotes) without changing game logic.

## Flow summary

1. Bootstrap: load `cards.json` via `CardFactory.loadBasic` (fallback to placeholders) and create `DeckManager`.
2. Setup: deal 3 cards to each player (choose stack), seed principality with legacy layout (`SetupService`).
3. Turn loop: `TurnManager.playTurn` orchestrates pre-roll, event die and production order (Brigand before production, else production first), action phase (numbered UI), replenish, exchange, and win check at VP>=7.

## Extensibility

- New effects or cards: add to `EffectCatalog` with a new key and a small `ICardEffect` implementation; JSON can reference via `effectKey`.
- New placements: implement `PlacementHandler` and register in `PlacementRegistry`.
- New eras/expansions: subclass or parameterize `GameConfig` and/or policy interfaces; extend `CardFactory` to include different themes and victory conditions.
- UI/Transport: swap `IInputService`/`IOutputService` implementations for console vs. socket vs. tests.

## Design choices vs. requirements

- Deck composition matches legacy: 49 center (24 regions, 9 settlements, 7 cities, 9 roads), 36 basic, 9 events; Yule positioned 4th from bottom; region dice assignment policy captured in config; four basic stacks split evenly via round-robin.
- Console text mirrors legacy key lines (production, event messages, board printout with full points line).
- One player per terminal: the current main runs two console players; networking hooks are isolated to the I/O layer to allow two separate terminals without changing controller/model code.

## Testing

- Tests under `src/tests` can use `MockIO` to script inputs and capture outputs. Keep effects small and deterministic. Prefer injecting `Randomizer`/`Dice` for reproducibility.

## SOLID and Booch metrics

- Single Responsibility: controllers coordinate flow; validators validate; effects encapsulate card logic; placement handlers only place; models store state.
- Open/Closed: `EffectCatalog` enables adding effects without modifying core; `PlacementRegistry` adds handlers without changing call sites.
- Liskov: `Card` hierarchy uses value semantics by name; `Deck<T>` generic over card types.
- Interface Segregation: minimal I/O and policy interfaces.
- Dependency Inversion: high-level controllers depend on abstractions (I/O, effects, policies), not concretions.

## Developer tips

- To add a new event card: implement an `ICardEffect`, register it in `EffectCatalog` with key `event:<name lowercased>`, and ensure `cards.json` has placement "Event" and either `effectKey` or compatible name.
- To add a new basic card: add effect, map it by name, ensure `cards.json` has a cost and `number` numeric; it will be picked up into the basic pool and auto-distributed.
- To change the victory condition: override `GameConfig.victoryPointsToWin()` or implement `VictoryPolicy` and inject where appropriate.

# from /home/simon/HomeExam-refactored-updated/src
rm -rf ../bin
mkdir -p ../bin

# compile with the gson.jar one level up
javac -d ../bin -cp "../gson.jar" $(find . -name "*.java")

# Terminal A (host)
java -cp "../bin:../gson.jar" src.network.Server

# Terminal B (client)
java -cp "../bin:../gson.jar" src.network.Server online