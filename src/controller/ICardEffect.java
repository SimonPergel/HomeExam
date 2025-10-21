
package src.controller;
import src.controller.GameContext;
public interface ICardEffect {
    void apply(GameContext ctx);
    default String description(){ return getClass().getSimpleName(); }
}
