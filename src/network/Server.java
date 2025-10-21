package src.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import src.controller.*;
import src.io.*;
import src.io.interfaces.*;
import src.model.Player;
import src.util.*;

/**
 * Minimal two-terminal server: run once as host ("Server"), once as client ("Server online").
 * - Terminal 1: java -cp gson.jar:. Server  (host) → local Player 1 uses console IO
 * - Terminal 2: java -cp gson.jar:. Server online (client) → Player 2 over socket IO
 */
public class Server {
	public static void main(String[] args) throws Exception {
		boolean online = args.length > 0 && args[0].equalsIgnoreCase("online");
		int port = 54545;

		var rng = new Randomizer();
		var cfg = new GameConfig();

		DeckManager decks;
		try {
			// Try to locate cards.json from common working directories (project root or src/)
			String[] candidates = new String[]{"./cards.json", "../cards.json", "../../cards.json"};
			String found = null;
			for (String c : candidates) {
				java.io.File f = new java.io.File(c);
				if (f.exists() && f.isFile()) { found = f.getCanonicalPath(); break; }
			}
			if (found == null) throw new RuntimeException("cards.json not found");
			var br = new CardFactory().loadBasic(found);
			decks = new DeckManager(rng, cfg, br);
		} catch (Exception ex) {
			decks = new DeckManager(rng, cfg);
		}

		var rules = new RuleValidator(cfg);
		var eventMgr = new EventManager(new Dice(rng), EffectCatalog.baseGame());
		var turns = new TurnManager(new Dice(rng), eventMgr, rules);

		if (!online) {
			// Host: accepts one client, maps IOs, runs the game
			try (ServerSocket ss = new ServerSocket(port)) {
				System.out.println("Waiting for player 2 on port " + port + " ...");
				Socket client = ss.accept();
				System.out.println("Player 2 connected.");

				IInputService hostIn = new ConsoleInput();
				IOutputService hostOut = new ConsoleOutput();
				IInputService clientIn = new SocketInput(client);
				IOutputService clientOut = new SocketOutput(client);

				var game = new GameController(turns, decks, rules, rng, cfg, hostIn, hostOut);

				var p1 = new Player("Player 1");
				var p2 = new Player("Player 2");

				// Register per-player IO
				game.registerPlayerIO(p1, new IPlayerIO(){ public IInputService in(){return hostIn;} public IOutputService out(){return hostOut;} });
				game.registerPlayerIO(p2, new IPlayerIO(){ public IInputService in(){return clientIn;} public IOutputService out(){return clientOut;} });

				// Legacy-like order: seed starter boards first, then opening hands P1 then P2, then print once to both terminals
				SetupService.seedIntroPrincipality(p1, 1);
				SetupService.seedIntroPrincipality(p2, 2);

				// Player 1 chooses entire opening hand first
				decks.dealOpeningHand(p1, hostIn, hostOut);
				// Then Player 2 chooses
				decks.dealOpeningHand(p2, clientIn, clientOut);

				// Print initial boards and hands for each player, like legacy
				hostOut.println("Opponent's starting board:");
				src.view.BoardPrinter.printPlayerBoard(p2, hostOut);
				hostOut.println("Your starting board:");
				src.view.BoardPrinter.printPlayerBoard(p1, hostOut);
				hostOut.println("Your starting hand:");
				printHand(p1, hostOut);

				clientOut.println("Opponent's starting board:");
				src.view.BoardPrinter.printPlayerBoard(p1, clientOut);
				clientOut.println("Your starting board:");
				src.view.BoardPrinter.printPlayerBoard(p2, clientOut);
				clientOut.println("Your starting hand:");
				printHand(p2, clientOut);

				Player current = rng.nextInt(2)==0? p1: p2;
				Player opp = current==p1? p2: p1;

				while(true){
					turns.playTurn(game, current, opp);
					if (rules.hasWon(current)) break;
					var tmp = current; current = opp; opp = tmp;
				}
			}
		} else {
			// Client: bridge server I/O to local terminal (stdin/stdout)
			try (Socket s = new Socket("localhost", port)) {
				java.io.BufferedReader sockIn = new java.io.BufferedReader(new java.io.InputStreamReader(s.getInputStream()));
				java.io.PrintWriter sockOut = new java.io.PrintWriter(new java.io.OutputStreamWriter(s.getOutputStream()), true);
				java.io.BufferedReader consoleIn = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));

				Thread tRead = new Thread(() -> {
					try {
						String line;
						while ((line = sockIn.readLine()) != null) {
							System.out.println(line);
						}
					} catch (IOException ignored) {}
				});
				Thread tWrite = new Thread(() -> {
					try {
						String line;
						while ((line = consoleIn.readLine()) != null) {
							sockOut.println(line);
						}
					} catch (IOException ignored) {}
				});
				tRead.setDaemon(true);
				tWrite.setDaemon(true);
				tRead.start();
				tWrite.start();

				// Wait until server closes connection
				tRead.join();
			} catch (Exception e) {
				System.err.println("Unable to connect to host on port " + port + ": " + e.getMessage());
			}
		}
	}

	private static void printHand(src.model.Player p, src.io.interfaces.IOutputService out){
		java.util.List<src.model.BasicCard> hand = p.getHandSnapshot();
		out.println("Hand ("+hand.size()+"): ");
		for (int i=0;i<hand.size();i++){
			var c = hand.get(i);
			String cost = c.getCost();
			if (cost != null && !cost.isBlank()) cost = src.util.CostParser.legacyToNewLetters(cost);
			String pts = src.controller.GameController.summarizePoints(c);
			out.println("  ["+i+"] " + c.getName() + (cost!=null && !cost.isBlank()? ("  {cost: "+cost+"}") : "") + (pts.isBlank()?"":"  {"+pts+"}"));
			if (c.getDescription()!=null && !c.getDescription().isBlank()) out.println("    " + c.getDescription());
		}
	}
}
