package src.controller.placement;

import java.util.Locale;
import java.util.function.BiPredicate;
import src.io.interfaces.IInputService;
import src.io.interfaces.IOutputService;

public final class CoordinatePrompter {
    private CoordinatePrompter(){}

    public static int[] askCoord(IInputService in, IOutputService out, String prompt,
                                 BiPredicate<Integer,Integer> validator, String help){
        while (true) {
            out.println(prompt);
            String s = in.readLine();
            if (s == null || s.equalsIgnoreCase("skip") || s.equalsIgnoreCase("cancel")) return null;
            s = s.replace(",", " ").replace("(", " ").replace(")", " ").toLowerCase(Locale.ROOT).trim();
            String[] parts = s.split("\\s+");
            if (parts.length >= 2) {
                try {
                    int r = Integer.parseInt(parts[0]);
                    int c = Integer.parseInt(parts[1]);
                    if (validator == null || validator.test(r,c)) return new int[]{r,c};
                    out.println("Invalid coordinate: " + r + " " + c + ". " + help);
                    continue;
                } catch (NumberFormatException ignore){}
            }
            out.println("Could not parse coordinates. " + help);
        }
    }

    public static Integer askInt(IInputService in, IOutputService out, String prompt, int min, int max){
        while (true){
            out.println(prompt + " ("+min+".."+max+")");
            String s = in.readLine();
            if (s == null || s.equalsIgnoreCase("skip") || s.equalsIgnoreCase("cancel")) return null;
            try {
                int v = Integer.parseInt(s.trim());
                if (v >= min && v <= max) return v;
                out.println("Out of range. Enter "+min+".."+max+".");
            } catch (NumberFormatException ignore){
                out.println("Not a number. Enter "+min+".."+max+".");
            }
        }
    }
}