package src;

import src.controller.*;
import src.io.*;
import src.io.interfaces.*;
import src.model.Player;
import src.util.*;
import src.controller.EffectCatalog;

public class Main {
    public static void main(String[] args){
        var rng = new Randomizer();
        var cfg = new GameConfig();
        var in = new ConsoleInput();
        var out = new ConsoleOutput();

        DeckManager decks;
        try {
            String jsonPath = new java.io.File("../cards.json").getCanonicalPath();
            var br = new CardFactory().loadBasic(jsonPath);
            decks = new DeckManager(rng, cfg, br);
            out.println("Loaded cards.json from: " + jsonPath);
        } catch (Exception ex) {
            out.println("cards.json load failed (" + ex.getMessage() + "). Using placeholder decks.");
            decks = new DeckManager(rng, cfg);
        }

        var rules = new RuleValidator(cfg);
        var eventMgr = new EventManager(new Dice(rng), EffectCatalog.baseGame());
        var turns = new TurnManager(new Dice(rng), eventMgr, rules);
        var game = new GameController(turns, decks, rules, rng, cfg, in, out);

        var p1 = new Player("Player 1");
        var p2 = new Player("Player 2");

        out.println("Opening hands...");
        decks.dealOpeningHand(p1, in, out);
        decks.dealOpeningHand(p2, in, out);
    // Base-game starting boards with explicit dice per player
    src.controller.SetupService.seedIntroPrincipality(p1, 1);
    src.controller.SetupService.seedIntroPrincipality(p2, 2);
        out.println("Opponent's starting board:");
        src.view.BoardPrinter.printPlayerBoard(p2, out);
        out.println("");
        out.println("Your starting board:");
        src.view.BoardPrinter.printPlayerBoard(p1, out);

        Player current = rng.nextInt(2)==0? p1: p2;
        Player opp = current==p1? p2: p1;
        out.println(current.getName()+" starts.");

        while(true){
            turns.playTurn(game, current, opp);
            if (rules.hasWon(current)) break;
            var tmp = current; current = opp; opp = tmp;
        }
    }
}