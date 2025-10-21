package src.controller;

import src.model.Player;
import src.io.interfaces.IInputService;
import src.io.interfaces.IOutputService;

/** Immutable context object handed to effects. */
public class GameContext {
    private final Player current;
    private final Player opponent;
    private final IInputService in;
    private final IOutputService out;
    private final IInputService inOpponent;
    private final IOutputService outOpponent;

    // NEW: expose core services so effects can use them
    private final DeckManager decks;
    private final RuleValidator rules;
    private final TurnState turn;

    public GameContext(
            Player current,
            Player opponent,
            IInputService in,
            IOutputService out,
            IInputService inOpponent,
            IOutputService outOpponent,
            DeckManager decks,
            RuleValidator rules,
            TurnState turn
    ) {
        this.current = current;
        this.opponent = opponent;
        this.in = in;
        this.out = out;
        this.inOpponent = inOpponent;
        this.outOpponent = outOpponent;
        this.decks = decks;
        this.rules = rules;
        this.turn = turn;
    }

    public Player current(){ return current; }
    public Player opponent(){ return opponent; }
    public IInputService in(){ return in; }
    public IOutputService out(){ return out; }
    public IInputService inOpponent(){ return inOpponent; }
    public IOutputService outOpponent(){ return outOpponent; }

    // NEW accessors used by your effects
    public DeckManager decks(){ return decks; }
    public RuleValidator rules(){ return rules; }
    public TurnState turn(){ return turn; }
}