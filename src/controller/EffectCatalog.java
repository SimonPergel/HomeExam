package src.controller;

import java.util.*;
import java.util.function.Supplier;

// Import effects from other packages:
import src.controller.actions.BrigittaTheWiseEffect;
import src.controller.actions.GoldsmithEffect;
import src.controller.actions.MerchantEffect;
import src.controller.actions.MerchantCaravanEffect;
import src.controller.actions.RelocationEffect;
import src.controller.actions.RoadBuildingEffect;
import src.controller.actions.HarvestEffect;

import src.controller.event.FeudEffect;
import src.controller.event.FraternalFeudsEffect;
import src.controller.event.InventionEventEffect;
import src.controller.event.TradeShipsRaceEffect;
import src.controller.event.TravelingMerchantEffect;
import src.controller.event.YearOfPlentyEffect;
import src.controller.event.YuleEffect;

import src.controller.eventDieEvents.BrigandEffect;
import src.controller.eventDieEvents.CelebrationEffect;
import src.controller.eventDieEvents.TradeEffect;
// If you later add a dedicated PlentifulHarvest effect under eventDieEvents,
// switch the mapping below from HarvestEffect to that class.

import src.controller.settlement.buildings.AbbeyEffect;
import src.controller.settlement.buildings.TollBridgeEffect;

// Common heroes
import src.controller.settlement.units.CommonHeroEffect;
// Common trade ships
import src.controller.settlement.units.CommonTradeShipEffect;
import src.controller.settlement.units.LargeTradeShipEffect;
import src.model.Resource;

/**
 * Central registry for mapping effect keys (e.g. "brigand", "event:year of plenty",
 * "goldsmith") to effect factories. Open for extension, closed for modification.
 */
public final class EffectCatalog {

    private final Map<String, Supplier<ICardEffect>> registry;

    private EffectCatalog(Map<String, Supplier<ICardEffect>> registry) {
        this.registry = Collections.unmodifiableMap(new LinkedHashMap<>(registry));
    }

    /** Returns an immutable, pre-wired catalog for the Base game. */
    public static EffectCatalog baseGame() {
        return builder()
                // Aliases for event die rolls -> canonical names
                .alias("1", "brigand")
                .alias("2", "trade")
                .alias("3", "celebration")
                .alias("4", "plentiful harvest")

                // Canonical event-die effects
                .register("brigand", BrigandEffect::new)
                .register("trade", TradeEffect::new)
                .register("celebration", CelebrationEffect::new)
                // Map die face 4 to your existing HarvestEffect (same behavior for base set)
                .register("plentiful harvest", HarvestEffect::new)
                .alias("harvest", "plentiful harvest")

                // --- Basic / action cards (names exactly as your CardCatalog emits) ---
                .register("merchant", MerchantEffect::new)
                .register("road building", RoadBuildingEffect::new)
                .register("merchant caravan", MerchantCaravanEffect::new)
                .register("goldsmith", GoldsmithEffect::new)
                .register("brigitta", BrigittaTheWiseEffect::new)
                .alias("brigitta, the wise woman", "brigitta")
                .alias("brigitta, the wise women", "brigitta")
                .register("relocation", RelocationEffect::new)
                .register("abbey", AbbeyEffect::new)
                // Generic buildings (placed via GenericBuildingPlacementHandler). Effects are mostly passive.
                .register("marketplace", () -> ctx -> ctx.out().println("Marketplace placed: passive effect (example: reminder only)."))
                .register("storehouse", () -> ctx -> ctx.out().println("Storehouse placed: passive effect (reminder only)."))
                .register("toll bridge", TollBridgeEffect::new)
                .alias("tool brige", "toll bridge")
                .register("brick factory", () -> ctx -> {})
                .register("grain mill", () -> ctx -> {})
                .register("iron foundry", () -> ctx -> {})
                .register("lumber camp", () -> ctx -> {})
                .register("weaver's shop", () -> ctx -> {})
                .alias("weavers shop", "weaver's shop")

                // --- Event cards (prefix "event:" to avoid collisions with basic cards) ---
                .register("event:feud", FeudEffect::new)
                .register("event:fraternal feuds", FraternalFeudsEffect::new)
                .register("event:invention", InventionEventEffect::new)
                .register("event:trade ships race", TradeShipsRaceEffect::new)
                .register("event:traveling merchant", TravelingMerchantEffect::new)
                .register("event:year of plenty", YearOfPlentyEffect::new)
                .register("event:yule", YuleEffect::new)
                .alias("event:the yule", "event:yule")

                // --- Common Heroes (core rulebook) ---
                .register("austin",   () -> new CommonHeroEffect(1, 2, "Austin"))
                .register("candamir", () -> new CommonHeroEffect(4, 1, "Candamir"))
                .register("harald",   () -> new CommonHeroEffect(2, 1, "Harald"))
                .register("inga",     () -> new CommonHeroEffect(1, 3, "Inga"))
                .register("osmund",   () -> new CommonHeroEffect(2, 2, "Osmund"))
                .register("siglind",  () -> new CommonHeroEffect(2, 3, "Siglind"))

                // --- Common Trade Ships (core rulebook) ---
                .register("lumber ship", () -> new CommonTradeShipEffect(Resource.WOOD,  "Lumber Ship"))
                .register("brick ship",  () -> new CommonTradeShipEffect(Resource.BRICK, "Brick Ship"))
                .register("grain ship",  () -> new CommonTradeShipEffect(Resource.WHEAT, "Grain Ship"))
                .register("ore ship",    () -> new CommonTradeShipEffect(Resource.ORE,   "Ore Ship"))
                .register("wool ship",   () -> new CommonTradeShipEffect(Resource.WOOL,  "Wool Ship"))
                .register("gold ship",   () -> new CommonTradeShipEffect(Resource.GOLD,  "Gold Ship"))
                .register("large trade ship", LargeTradeShipEffect::new)
                .build();
    }

    public static Builder builder() { return new Builder(); }

    /** Get an effect for a key or throw if missing. */
    public ICardEffect forKey(String key) {
        Objects.requireNonNull(key, "key");
        var fn = registry.get(normalize(key));
        if (fn == null) throw new IllegalArgumentException("No effect registered for key: " + key);
        return fn.get();
    }

    /** Try to get an effect; empty if key isn't registered. */
    public Optional<ICardEffect> tryGet(String key) {
        if (key == null) return Optional.empty();
        var fn = registry.get(normalize(key));
        return (fn == null) ? Optional.empty() : Optional.of(fn.get());
    }

    /** True if a key (or alias) exists. */
    public boolean contains(String key) {
        return key != null && registry.containsKey(normalize(key));
    }

    /** View of all registered keys (aliases included). */
    public Set<String> keys() { return registry.keySet(); }

    /** Normalize keys to make lookups case/space-insensitive. */
    private static String normalize(String key) { return key.trim().toLowerCase(Locale.ROOT); }

    // -------- Builder --------
    public static final class Builder {
        private final Map<String, Supplier<ICardEffect>> canon = new LinkedHashMap<>();
        private final Map<String, String> aliases = new LinkedHashMap<>();

        /** Register a canonical key -> factory. */
        public Builder register(String key, Supplier<ICardEffect> factory) {
            canon.put(normalize(key), Objects.requireNonNull(factory));
            return this;
        }

        /** Map an alias to a canonical key (the canonical must be registered by build()). */
        public Builder alias(String alias, String canonicalKey) {
            aliases.put(normalize(alias), normalize(canonicalKey));
            return this;
        }

        public EffectCatalog build() {
            Map<String, Supplier<ICardEffect>> merged = new LinkedHashMap<>();
            merged.putAll(canon); // canonical entries
            for (var e : aliases.entrySet()) {
                var target = canon.get(e.getValue());
                if (target != null) merged.put(e.getKey(), target);
            }
            return new EffectCatalog(merged);
        }

        private static String normalize(String s) { return s.trim().toLowerCase(Locale.ROOT); }
    }
}