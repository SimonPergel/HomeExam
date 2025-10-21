package src.controller.phases;

import java.util.ArrayList;
import java.util.List;

import src.controller.GameContext;
import src.controller.GameController;
import src.controller.actions.PreRollEffect;
import src.model.BasicCard;
import src.model.Player;

public final class PreRollPhase {

    public void run(GameController game, Player current, Player opponent) {
        // Legacy-aligned behavior for this refactor: skip pre-roll prompts entirely
        // so turns proceed immediately to the dice process without player input.
        // (Cards like Brigitta can be enabled later behind an option.)
        return;
    }

    private int parseInt(String s, int min, int max) {
        try {
            int v = Integer.parseInt(s == null ? "" : s.trim());
            if (v >= min && v <= max) return v;
        } catch (NumberFormatException ignored) {}
        return 0;
    }
}