package src.controller;

import java.util.Optional;

/** Per-turn scratchpad (for pre-roll effects like Brigitta). */
public final class TurnState {
    private boolean diceRolled = false;
    private Optional<Integer> productionOverride = Optional.empty(); // 1..6

    public boolean areDiceRolled() { return diceRolled; }
    public void markDiceRolled() { this.diceRolled = true; }

    public Optional<Integer> getProductionOverride() { return productionOverride; }
    public void setProductionOverride(Integer value) {
        this.productionOverride = Optional.ofNullable(value);
    }
    public void clearProductionOverride() { this.productionOverride = Optional.empty(); }
}
