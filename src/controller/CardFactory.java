package src.controller;

import com.google.gson.*;
import java.io.*;
import java.util.*;
import java.util.Locale;
import src.model.*;
import src.controller.actions.*;

/** Loads cards from cards.json and builds piles/stacks for the Basic set. */
public class CardFactory {

    public static class BuildResult {
        public final List<Card> regions;
        public final List<Card> roads;
        public final List<Card> settlements;
        public final List<Card> cities;
        public final List<EventCard> events;
        public final List<BasicCard> basicPool; // to be split into 4 stacks

        public BuildResult(List<Card> regions, List<Card> roads, List<Card> settlements,
                           List<Card> cities, List<EventCard> events, List<BasicCard> basicPool) {
            this.regions = regions; this.roads = roads; this.settlements = settlements;
            this.cities = cities; this.events = events; this.basicPool = basicPool;
        }
    }

    public BuildResult loadBasic(String jsonPath) throws IOException {
        // MUST be outside try so we can use it after the try block
        List<Card> allBasic = new ArrayList<>();

        try (FileReader fr = new FileReader(jsonPath)) {
            JsonElement root = JsonParser.parseReader(fr);
            if (!root.isJsonArray())
                throw new IOException("cards.json: expected top-level array");
            JsonArray arr = root.getAsJsonArray();

            for (JsonElement el : arr) {
                if (!el.isJsonObject()) continue;
                JsonObject o = el.getAsJsonObject();

                String theme = gs(o, "theme");
                if (theme == null || !theme.toLowerCase().contains("basic")) continue; // only Basic set

                String name      = gs(o, "name");
                String type      = gs(o, "type");
                String placement = gs(o, "placement");
                String cost      = gs(o, "cost");
                int number       = gi(o, "number", 1);

                // expand duplicates by "number"
                for (int i = 0; i < number; i++) {
                    Card c;
                    if ("Event".equalsIgnoreCase(placement)) {
                        // (unchanged) resolve event effect via effectKey or "event:<name>"
                        String ek  = gs(o, "effectKey");
                        String key = (ek != null && !ek.isBlank())
                            ? ek.toLowerCase(Locale.ROOT)
                            : "event:" + name.toLowerCase(Locale.ROOT);
                        var eff = EffectCatalog.baseGame().tryGet(key).orElse(null);
                        c = (eff != null) ? new EventCard(name, eff) : new EventCard(name);

                    } else if ("Center Card".equalsIgnoreCase(placement) || "Center Card".equalsIgnoreCase(type)) {
                        // Your schema: use NAME to tell which center stack it belongs to
                        if ("Road".equalsIgnoreCase(name)) {
                            c = new CenterCard(name, "ROAD");
                        } else if ("Settlement".equalsIgnoreCase(name)) {
                            c = new CenterCard(name, "SETTLEMENT");
                        } else if ("City".equalsIgnoreCase(name)) {
                            c = new CenterCard(name, "CITY");
                        } else {
                            // everything else in Center Card is a REGION (Field, Forest, Hill, Mountain, Pasture, Gold Field, etc.)
                            c = new CenterCard(name, "REGION");
                        }

                    } else {
                        // Basic draw cards: resolve effect via effectKey or lowercased name
                        String ek  = gs(o, "effectKey");
                        String key = (ek != null && !ek.isBlank())
                            ? ek.toLowerCase(Locale.ROOT)
                            : name.toLowerCase(Locale.ROOT);
                        var eff = EffectCatalog.baseGame().tryGet(key).orElse(null);
                        BasicCard bc = new BasicCard(name, eff);
                        if (cost != null) bc.setCost(cost);
                        String cardText = gs(o, "cardText");
                        if (cardText != null) bc.setDescription(cardText);
                        c = bc;
                    }

                    // IMPORTANT: add inside the loop (for each copy)
                    allBasic.add(c);
                }
            }
        }

        // ----- Partition into piles (center) -----
        List<Card> roads       = popByName(allBasic, "Road");
        List<Card> settlements = popByName(allBasic, "Settlement");
        List<Card> cities      = popByName(allBasic, "City");
        List<Card> regions     = popByType(allBasic, CenterCard.class, "REGION");

        // ----- Events (already typed as EventCard) -----
        List<EventCard> events = new ArrayList<>();
        for (Iterator<Card> it = allBasic.iterator(); it.hasNext();) {
            Card c = it.next();
            if (c instanceof EventCard) { events.add((EventCard)c); it.remove(); }
        }

        // Move Yule to 4th from bottom (after shuffle)
        EventCard yule = null;
        for (Iterator<EventCard> it = events.iterator(); it.hasNext();) {
            EventCard e = it.next();
            if ("Yule".equalsIgnoreCase(e.getName())) { yule = e; it.remove(); break; }
        }
        Collections.shuffle(events);
        if (yule != null && events.size() >= 3) {
            events.add(Math.max(0, events.size() - 3), yule);
        }

        // ----- Remaining are basic draw cards -----
        List<BasicCard> basicPool = new ArrayList<>();
        for (Card c: allBasic) if (c instanceof BasicCard) basicPool.add((BasicCard)c);
        Collections.shuffle(basicPool);

        return new BuildResult(regions, roads, settlements, cities, events, basicPool);
    }

    private static String gs(JsonObject o, String k){
        JsonElement e = o.get(k);
        return (e==null || e.isJsonNull())? null : e.getAsString();
    }
    private static int gi(JsonObject o, String k, int def){
        JsonElement e = o.get(k);
        if (e==null || e.isJsonNull()) return def;
        try { return e.getAsInt(); } catch(Exception ex){ return def; }
    }
    private static boolean equalsAny(String s, String... options){
        if (s==null) return false;
        for (String o: options) if (s.equalsIgnoreCase(o)) return true;
        return false;
    }
    private static List<Card> popByName(List<Card> pool, String name){
        List<Card> res = new ArrayList<>();
        for (Iterator<Card> it = pool.iterator(); it.hasNext();) {
            Card c = it.next();
            if (name.equalsIgnoreCase(c.getName())) { res.add(c); it.remove(); }
        }
        return res;
    }
    private static List<Card> popByType(List<Card> pool, Class<?> klass, String category){
        List<Card> res = new ArrayList<>();
        for (Iterator<Card> it = pool.iterator(); it.hasNext();) {
            Card c = it.next();
            if (klass.isInstance(c)) {
                if (c instanceof CenterCard) {
                    CenterCard cc = (CenterCard) c;
                    if (!category.equalsIgnoreCase(cc.getCategory())) continue;
                }
                res.add(c); it.remove();
            }
        }
        return res;
    }

    // When loading basic cards, set their cost and effect
    private List<BasicCard> expandBasic(List<JsonObject> entries) {
        List<BasicCard> out = new ArrayList<>();
        for (JsonObject o : entries) {
            String name = s(o, "name");
            String cost = s(o, "cost"); // may be "", null, or like "BGO"
            int copies = i(o, "number", 1);
            for (int j = 0; j < copies; j++) {
                String ek  = s(o, "effectKey");
                String key = (ek != null && !ek.isBlank())
                        ? ek.toLowerCase(Locale.ROOT)
                        : name.toLowerCase(Locale.ROOT);
                var eff = EffectCatalog.baseGame().tryGet(key).orElse(null);
                var bc = new BasicCard(name, eff);
                bc.setCost(cost);
                out.add(bc);
            }
        }
        return out;
    }
    private static String s(com.google.gson.JsonObject o, String key) {
        com.google.gson.JsonElement e = o.get(key);
        return (e == null || e.isJsonNull()) ? "" : e.getAsString();
    }

    private static int i(com.google.gson.JsonObject o, String key, int def) {
        com.google.gson.JsonElement e = o.get(key);
        if (e == null || e.isJsonNull()) return def;
        try { return e.getAsInt(); } catch (Exception ex) { return def; }
    }
}