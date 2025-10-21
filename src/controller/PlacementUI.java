package src.controller;

import java.util.Locale;

import src.io.interfaces.IInputService;
import src.io.interfaces.IOutputService;
import src.model.Player;
import src.model.Principality;
import src.view.BoardPrinter;

/** Minimal UI helper for placing center cards by coordinates. */
public final class PlacementUI {
    private PlacementUI(){}

    

    /** Parse "row col" (also supports "row,col"). Enforces inclusive ranges. */
    private static int[] askCoord(IInputService in, IOutputService out,
                                  int minRow, int maxRow, int minCol, int maxCol) {
        while (true) {
            out.println("Enter row col:");
            String s = in.readLine();
            if (s == null) return new int[]{minRow, minCol};
            s = s.trim().toLowerCase(Locale.ROOT).replace(",", " ");
            String[] parts = s.split("\\s+");
            if (parts.length >= 2) {
                try {
                    int r = Integer.parseInt(parts[0]);
                    int c = Integer.parseInt(parts[1]);
                    if (r >= minRow && r <= maxRow && c >= minCol && c <= maxCol) {
                        return new int[]{r, c};
                    }
                } catch (NumberFormatException ignored) {}
            }
            out.println("Invalid coordinate. Expected numbers in range: row " + minRow + ".." + maxRow
                        + ", col " + minCol + ".." + maxCol + ".");
        }
    }
}
