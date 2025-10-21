package src.controller.event;

import src.controller.GameContext;
import src.controller.ICardEffect;

/**
 * Traveling Merchant (2):
 * You also may use any GOLD you have received via the current production die roll.
 *
 * NOTE: If your rules normally restrict using newly-produced gold immediately,
 * set a flag here on the player/turn context. For now we log the allowance.
 */
public class TravelingMerchantEffect implements ICardEffect {
    @Override public void apply(GameContext ctx) {
        ctx.out().println("Event: Traveling Merchant — You may also use any GOLD received from the current production die roll. (TODO: add a 'goldUsableThisTurn' flag if needed.)");
    }
}
