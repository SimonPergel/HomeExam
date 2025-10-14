package src.model;

import java.util.*;

public class Principality {
    private List<List<Card>> grid = new ArrayList<>();

    public Principality() {
        for (int r = 0; r < 5; r++) {
            List<Card> row = new ArrayList<>();
            for (int c = 0; c < 5; c++) row.add(null);
            grid.add(row);
        }
    }

    public Card getCard(int r, int c) {
        if (r < 0 || c < 0 || r >= grid.size() || c >= grid.get(0).size()) return null;
        return grid.get(r).get(c);
    }

    public void placeCard(int r, int c, Card card) {
        while (r >= grid.size()) {
            List<Card> row = new ArrayList<>();
            for (int i = 0; i < grid.get(0).size(); i++) row.add(null);
            grid.add(row);
        }
        grid.get(r).set(c, card);
    }
}
