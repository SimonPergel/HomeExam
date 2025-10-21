package src.controller;

import java.util.Locale;
import src.controller.eventDieEvents.EventCardEffect;

/**
 * Deprecated compatibility shim to allow a gradual migration.
 * Delegates to EffectCatalog.baseGame(); falls back to EventCardEffect
 * if a name isn't registered.
 */
@Deprecated
public final class EffectRegistry {

    private static final EffectCatalog CATALOG = EffectCatalog.baseGame();

    private EffectRegistry() {}

    public static ICardEffect forName(String cardName) {
        String key = cardName == null ? "" : cardName.toLowerCase(Locale.ROOT).trim();
        return CATALOG.tryGet(key).orElseGet(() -> new EventCardEffect(cardName));
    }
}

