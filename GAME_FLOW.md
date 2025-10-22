# Game Flow Diagram

This document illustrates the game flow and control flow through the application.

## Main Game Flow

```mermaid
flowchart TD
    Start([Start Game]) --> Init[Initialize Game<br/>GameController]
    Init --> Setup[Setup Service<br/>Create Players, Decks, Boards]
    Setup --> GameLoop{Game Loop}
    
    GameLoop --> TurnStart[Start Turn<br/>TurnManager]
    TurnStart --> EventRoll[Roll Event Die]
    
    EventRoll --> EventCheck{Event Result?}
    EventCheck -->|Brigand| BrigandEffect[Execute Brigand Attack]
    EventCheck -->|Trade| TradeEffect[Execute Trade]
    EventCheck -->|Celebration| CelebrationEffect[Execute Celebration]
    EventCheck -->|Event Card| DrawEvent[Draw Event Card]
    
    BrigandEffect --> PreRoll
    TradeEffect --> PreRoll
    CelebrationEffect --> PreRoll
    DrawEvent --> ExecuteEvent[Execute Event Effect]
    ExecuteEvent --> PreRoll
    
    PreRoll[Pre-Roll Phase<br/>Play Pre-Roll Cards] --> ProductionRoll[Roll Production Die]
    ProductionRoll --> Production[Production Phase<br/>Generate Resources]
    
    Production --> ActionPhase[Action Phase]
    ActionPhase --> ActionLoop{Player Actions}
    
    ActionLoop -->|Build| BuildAction[Build Action<br/>Place Settlement/Road/City]
    ActionLoop -->|Play Card| PlayCard[Play Action Card]
    ActionLoop -->|Trade| TradeAction[Trade with Bank/Opponent]
    ActionLoop -->|Pass| EndTurn
    
    BuildAction --> PlacementValidation{Valid Placement?}
    PlacementValidation -->|Yes| ExecuteBuild[Place Structure<br/>Update Board]
    PlacementValidation -->|No| ActionLoop
    ExecuteBuild --> UpdateVP[Update Victory Points]
    UpdateVP --> ActionLoop
    
    PlayCard --> CardEffect{Card Type?}
    CardEffect -->|Action| ExecuteAction[Execute Card Effect]
    CardEffect -->|Building| PlaceBuilding[Place Building Effect]
    CardEffect -->|Unit| PlaceUnit[Place Unit Effect]
    
    ExecuteAction --> ActionLoop
    PlaceBuilding --> ActionLoop
    PlaceUnit --> ActionLoop
    
    TradeAction --> ExecuteTrade[Exchange Resources]
    ExecuteTrade --> ActionLoop
    
    EndTurn[End Turn] --> CheckWin{Victory<br/>Condition Met?}
    CheckWin -->|Yes| Winner[Declare Winner<br/>End Game]
    CheckWin -->|No| NextPlayer[Next Player]
    NextPlayer --> GameLoop
    
    Winner --> End([Game Over])
    
    style Start fill:#90EE90
    style End fill:#FFB6C1
    style GameLoop fill:#FFD700
    style CheckWin fill:#87CEEB
    style Winner fill:#FF6347
```

## Detailed Turn Flow

```mermaid
flowchart TD
    TurnStart([Turn Start]) --> CurrentPlayer[Get Current Player<br/>TurnManager]
    CurrentPlayer --> EventDieRoll[Roll Event Die<br/>Dice.roll]
    
    EventDieRoll --> ParseEvent{Parse Event Result}
    ParseEvent -->|1-3| Brigand[Brigand Attack<br/>BrigandEffect]
    ParseEvent -->|4| Trade[Trade Event<br/>TradeEffect]
    ParseEvent -->|5| Celebration[Celebration<br/>CelebrationEffect]
    ParseEvent -->|6| EventCard[Draw & Execute<br/>EventCardEffect]
    
    Brigand --> BrigandLogic[Steal Resource<br/>from Opponent]
    Trade --> TradeLogic[Execute Trade Rules]
    Celebration --> CelebrationLogic[Grant Celebration Bonus]
    EventCard --> DrawCard[Draw from Event Deck]
    DrawCard --> EventCardLogic{Event Type?}
    
    EventCardLogic -->|Invention| InventionEffect[Free Upgrade]
    EventCardLogic -->|Trade Ships| TradeShipsEffect[Trade Ships Race]
    EventCardLogic -->|Year of Plenty| YearOfPlentyEffect[Free Resources]
    EventCardLogic -->|Traveling Merchant| MerchantEffect[Merchant Visit]
    
    BrigandLogic --> PreRollPhase
    TradeLogic --> PreRollPhase
    CelebrationLogic --> PreRollPhase
    InventionEffect --> PreRollPhase
    TradeShipsEffect --> PreRollPhase
    YearOfPlentyEffect --> PreRollPhase
    MerchantEffect --> PreRollPhase
    
    PreRollPhase[Pre-Roll Phase] --> CheckPreRoll{Has Pre-Roll Cards?}
    CheckPreRoll -->|Yes| PlayPreRoll[Play Pre-Roll Card<br/>e.g., Brigitta]
    CheckPreRoll -->|No| ProductionDie
    PlayPreRoll --> ProductionDie
    
    ProductionDie[Roll Production Die] --> ProductionValue{Die Value}
    ProductionValue -->|1-6| GenerateResources[Generate Resources<br/>from Regions]
    
    GenerateResources --> CheckBuildings{Active Buildings?}
    CheckBuildings -->|Storehouse| StorehouseBonus[+1 Bonus Resource]
    CheckBuildings -->|Marketplace| MarketplaceBonus[Trade Discount]
    CheckBuildings -->|Abbey| AbbeyBonus[Card Draw Bonus]
    CheckBuildings -->|None| ActionPhaseStart
    
    StorehouseBonus --> ActionPhaseStart
    MarketplaceBonus --> ActionPhaseStart
    AbbeyBonus --> ActionPhaseStart
    
    ActionPhaseStart[Action Phase Start] --> ActionMenu{Choose Action}
    
    ActionMenu -->|1| BuildStructure[Build Structure]
    ActionMenu -->|2| PlayActionCard[Play Action Card]
    ActionMenu -->|3| TradeResources[Trade Resources]
    ActionMenu -->|4| EndTurnChoice[End Turn]
    
    BuildStructure --> ChooseBuild{Build Type?}
    ChooseBuild -->|Settlement| SettlementPlace[Settlement Placement]
    ChooseBuild -->|Road| RoadPlace[Road Placement]
    ChooseBuild -->|City| CityPlace[City Upgrade]
    ChooseBuild -->|Building| BuildingPlace[Building Placement]
    
    SettlementPlace --> ValidateSettle{Valid?}
    ValidateSettle -->|Yes| PaySettle[Pay Cost]
    ValidateSettle -->|No| ActionMenu
    PaySettle --> PlaceSettle[Place Settlement]
    PlaceSettle --> AddVP1[+1 VP]
    AddVP1 --> ActionMenu
    
    RoadPlace --> ValidateRoad{Valid?}
    ValidateRoad -->|Yes| PayRoad[Pay Cost]
    ValidateRoad -->|No| ActionMenu
    PayRoad --> PlaceRoad[Place Road]
    PlaceRoad --> ActionMenu
    
    CityPlace --> ValidateCity{Has Settlement?}
    ValidateCity -->|Yes| PayCity[Pay Upgrade Cost]
    ValidateCity -->|No| ActionMenu
    PayCity --> UpgradeCity[Upgrade to City]
    UpgradeCity --> AddVP2[+1 VP]
    AddVP2 --> ActionMenu
    
    BuildingPlace --> ValidateBuilding{Valid Position?}
    ValidateBuilding -->|Yes| PayBuilding[Pay Cost]
    ValidateBuilding -->|No| ActionMenu
    PayBuilding --> PlaceBuilding[Place Building]
    PlaceBuilding --> ActionMenu
    
    PlayActionCard --> SelectCard{Select Card}
    SelectCard --> ValidateCard{Can Play?}
    ValidateCard -->|Yes| ExecuteCardEffect[Execute Effect]
    ValidateCard -->|No| ActionMenu
    
    ExecuteCardEffect --> CardType{Card Type?}
    CardType -->|Goldsmith| GoldsmithExec[Exchange Gold for VP]
    CardType -->|Merchant| MerchantExec[Trade Advantage]
    CardType -->|Harvest| HarvestExec[Resource Generation]
    CardType -->|Scout| ScoutExec[Card Draw]
    
    GoldsmithExec --> ActionMenu
    MerchantExec --> ActionMenu
    HarvestExec --> ActionMenu
    ScoutExec --> ActionMenu
    
    TradeResources --> TradeType{Trade With?}
    TradeType -->|Bank| BankTrade[4:1 or 3:1 Trade]
    TradeType -->|Opponent| PlayerTrade[Negotiate Trade]
    
    BankTrade --> ExecuteTradeBank[Exchange Resources]
    PlayerTrade --> ExecuteTradePlayer[Exchange Resources]
    ExecuteTradeBank --> ActionMenu
    ExecuteTradePlayer --> ActionMenu
    
    EndTurnChoice --> VictoryCheck{Has 7+ VP?}
    VictoryCheck -->|Yes| DeclareWinner([Victory!])
    VictoryCheck -->|No| NextTurn[Next Player's Turn]
    
    NextTurn --> TurnEnd([Turn End])
    
    style TurnStart fill:#90EE90
    style TurnEnd fill:#FFB6C1
    style DeclareWinner fill:#FF6347
    style ActionMenu fill:#FFD700
    style VictoryCheck fill:#87CEEB
```

## Card Effect Execution Flow

```mermaid
flowchart TD
    PlayCard([Play Card]) --> GetCard[Retrieve Card from Hand]
    GetCard --> CheckCost{Can Afford?}
    CheckCost -->|No| Reject[Reject Action]
    CheckCost -->|Yes| PayCost[Deduct Resources]
    
    PayCost --> GetEffect[Get Effect from Registry]
    GetEffect --> EffectType{Effect Type?}
    
    EffectType -->|ICardEffect| ExecuteEffect[Execute Effect]
    
    ExecuteEffect --> EffectCategory{Category?}
    
    EffectCategory -->|Resource| ResourceEffect[Modify Resources]
    EffectCategory -->|Placement| PlacementEffect[Place Structure]
    EffectCategory -->|Victory| VictoryEffect[Modify VP]
    EffectCategory -->|Trade| TradeEffect[Trade Action]
    EffectCategory -->|Draw| DrawEffect[Draw Cards]
    
    ResourceEffect --> ResourceLogic{Resource Type?}
    ResourceLogic -->|Generate| AddResources[Add Resources to Player]
    ResourceLogic -->|Convert| ConvertResources[Exchange Resources]
    ResourceLogic -->|Steal| StealResources[Take from Opponent]
    
    AddResources --> UpdateState
    ConvertResources --> UpdateState
    StealResources --> UpdateState
    
    PlacementEffect --> SelectPosition[Choose Position]
    SelectPosition --> ValidatePlacement{Valid?}
    ValidatePlacement -->|Yes| PlaceStructure[Place on Board]
    ValidatePlacement -->|No| Reject
    PlaceStructure --> UpdateBoard[Update Principality]
    UpdateBoard --> UpdateState
    
    VictoryEffect --> ModifyVP[Modify Victory Points]
    ModifyVP --> UpdateState
    
    TradeEffect --> TradeLogic[Execute Trade]
    TradeLogic --> UpdateState
    
    DrawEffect --> DrawCards[Draw from Deck]
    DrawCards --> AddToHand[Add to Hand]
    AddToHand --> UpdateState
    
    UpdateState[Update Game State] --> Notify[Notify Players]
    Notify --> CheckTriggers{Triggered Effects?}
    CheckTriggers -->|Yes| ExecuteTrigger[Execute Triggered Effect]
    CheckTriggers -->|No| Complete
    ExecuteTrigger --> Complete
    
    Complete([Effect Complete])
    Reject --> Complete
    
    style PlayCard fill:#90EE90
    style Complete fill:#FFB6C1
    style Reject fill:#FF6347
```

## Placement Validation Flow

```mermaid
flowchart TD
    RequestPlace([Request Placement]) --> PlaceType{Structure Type?}
    
    PlaceType -->|Settlement| SettleHandler[SettlementPlacementHandler]
    PlaceType -->|Road| RoadHandler[RoadPlacementHandler]
    PlaceType -->|City| CityHandler[CityPlacementHandler]
    PlaceType -->|Building| BuildingHandler[GenericBuildingPlacementHandler]
    PlaceType -->|Abbey| AbbeyHandler[AbbeyPlacementHandler]
    PlaceType -->|Hero| HeroHandler[HeroPlacementHandler]
    
    SettleHandler --> SettleRules{Check Rules}
    SettleRules --> EmptyIntersection{Intersection Empty?}
    EmptyIntersection -->|No| InvalidSettle[Invalid Placement]
    EmptyIntersection -->|Yes| HasRoad{Adjacent Road?}
    HasRoad -->|No| InvalidSettle
    HasRoad -->|Yes| NoNeighbor{No Adjacent Settlement?}
    NoNeighbor -->|No| InvalidSettle
    NoNeighbor -->|Yes| ValidSettle[Valid Placement]
    
    RoadHandler --> RoadRules{Check Rules}
    RoadRules --> EmptyEdge{Edge Empty?}
    EmptyEdge -->|No| InvalidRoad[Invalid Placement]
    EmptyEdge -->|Yes| ConnectedStructure{Connected to Own Structure?}
    ConnectedStructure -->|No| InvalidRoad
    ConnectedStructure -->|Yes| ValidRoad[Valid Placement]
    
    CityHandler --> CityRules{Check Rules}
    CityRules --> HasSettlement{Settlement Exists?}
    HasSettlement -->|No| InvalidCity[Invalid Upgrade]
    HasSettlement -->|Yes| CanAfford{Can Afford Cost?}
    CanAfford -->|No| InvalidCity
    CanAfford -->|Yes| ValidCity[Valid Upgrade]
    
    BuildingHandler --> BuildingRules{Check Rules}
    BuildingRules --> HasSlot{Building Slot Available?}
    HasSlot -->|No| InvalidBuilding[Invalid Placement]
    HasSlot -->|Yes| NoConflict{No Duplicate?}
    NoConflict -->|No| InvalidBuilding
    NoConflict -->|Yes| ValidBuilding[Valid Placement]
    
    AbbeyHandler --> AbbeyRules{Check Rules}
    AbbeyRules --> CityLevel{Has City?}
    CityLevel -->|No| InvalidAbbey[Invalid Placement]
    CityLevel -->|Yes| AbbeySlot{Abbey Slot Free?}
    AbbeySlot -->|No| InvalidAbbey
    AbbeySlot -->|Yes| ValidAbbey[Valid Placement]
    
    HeroHandler --> HeroRules{Check Rules}
    HeroRules --> HeroSlot{Hero Slot Available?}
    HeroSlot -->|No| InvalidHero[Invalid Placement]
    HeroSlot -->|Yes| ValidHero[Valid Placement]
    
    ValidSettle --> ExecutePlace
    ValidRoad --> ExecutePlace
    ValidCity --> ExecutePlace
    ValidBuilding --> ExecutePlace
    ValidAbbey --> ExecutePlace
    ValidHero --> ExecutePlace
    
    InvalidSettle --> RejectPlace
    InvalidRoad --> RejectPlace
    InvalidCity --> RejectPlace
    InvalidBuilding --> RejectPlace
    InvalidAbbey --> RejectPlace
    InvalidHero --> RejectPlace
    
    ExecutePlace[Execute Placement] --> UpdatePrincipality[Update Principality]
    UpdatePrincipality --> UpdateVictory[Update Victory Points]
    UpdateVictory --> Success([Placement Success])
    
    RejectPlace[Reject Placement] --> Failure([Placement Failed])
    
    style RequestPlace fill:#90EE90
    style Success fill:#FFB6C1
    style Failure fill:#FF6347
    style ExecutePlace fill:#FFD700
```

## Initialization Flow

```mermaid
flowchart TD
    Start([Start Application]) --> LoadConfig[Load GameConfig]
    LoadConfig --> InitController[Initialize GameController]
    InitController --> CreateIO[Create IO Services<br/>Console/Socket/Mock]
    
    CreateIO --> SetupGame[SetupService.initialize]
    SetupGame --> CreatePlayers[Create 2 Players]
    CreatePlayers --> InitPrincipality[Initialize Principalities]
    
    InitPrincipality --> CreateDecks[Create Decks]
    CreateDecks --> LoadCards[Load cards.json]
    LoadCards --> ParseCards[CardFactory.parse]
    ParseCards --> CreateBasicDeck[Create Basic Deck]
    CreateBasicDeck --> CreateEventDeck[Create Event Deck]
    CreateEventDeck --> CreateCenterDeck[Create Center Deck]
    
    CreateCenterDeck --> ShuffleDecks[Shuffle All Decks]
    ShuffleDecks --> InitialDraw[Deal Starting Hands]
    
    InitialDraw --> RegisterEffects[Register Effects<br/>EffectRegistry]
    RegisterEffects --> RegActions[Register Action Effects]
    RegisterEffects --> RegEvents[Register Event Effects]
    RegisterEffects --> RegBuildings[Register Building Effects]
    
    RegActions --> SetupComplete
    RegEvents --> SetupComplete
    RegBuildings --> SetupComplete
    
    SetupComplete[Setup Complete] --> DisplayBoard[Display Initial Board<br/>BoardPrinter]
    DisplayBoard --> ReadyToPlay([Ready to Play])
    
    style Start fill:#90EE90
    style ReadyToPlay fill:#FFB6C1
    style SetupComplete fill:#FFD700
```

## Multiplayer Network Flow

```mermaid
flowchart TD
    StartMP([Start Multiplayer]) --> HostOrJoin{Host or Join?}
    
    HostOrJoin -->|Host| CreateServer[Create Server]
    HostOrJoin -->|Join| ConnectClient[Connect as Client]
    
    CreateServer --> ServerInit[Server.initialize]
    ServerInit --> ListenPort[Listen on Port]
    ListenPort --> WaitConnection[Wait for Connection]
    
    WaitConnection --> ClientConnects[Client Connects]
    ClientConnects --> CreateClientConn[Create ClientConnection]
    CreateClientConn --> CreateOnlinePlayer[Create OnlinePlayer]
    
    ConnectClient --> NetworkService[NetworkService.connect]
    NetworkService --> EstablishConn[Establish Connection]
    EstablishConn --> CreateSocketIO[Create SocketInput/Output]
    
    CreateOnlinePlayer --> SetupSocketIO[Setup Socket IO]
    CreateSocketIO --> JoinGame
    SetupSocketIO --> JoinGame
    
    JoinGame[Both Players Ready] --> SyncGame[Synchronize Game State]
    SyncGame --> StartNetworkGame[Start Networked Game]
    
    StartNetworkGame --> GameLoopMP[Game Loop]
    GameLoopMP --> PlayerAction{Current Player}
    
    PlayerAction -->|Local| LocalAction[Execute Locally]
    PlayerAction -->|Remote| SendAction[Send Action via Socket]
    
    LocalAction --> BroadcastState[Broadcast State Update]
    SendAction --> ReceiveAction[Receive at Server]
    ReceiveAction --> ValidateAction{Valid?}
    ValidateAction -->|Yes| ExecuteRemote[Execute Action]
    ValidateAction -->|No| RejectAction[Reject & Notify]
    
    ExecuteRemote --> BroadcastState
    BroadcastState --> UpdateBothClients[Update All Clients]
    UpdateBothClients --> GameLoopMP
    
    RejectAction --> NotifyClient[Notify Client]
    NotifyClient --> GameLoopMP
    
    style StartMP fill:#90EE90
    style JoinGame fill:#FFD700
    style GameLoopMP fill:#87CEEB
```
