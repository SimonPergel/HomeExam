package src.controller;

import java.util.Optional;

/** Per-turn scratchpad (for pre-roll effects like Brigitta and Scout). */
public final class TurnState {
    private boolean diceRolled = false;
    private Optional<Integer> productionOverride = Optional.empty(); // 1..6
    private boolean scoutActive = false; // Scout card was played

    public boolean areDiceRolled() { return diceRolled; }
    public void markDiceRolled() { this.diceRolled = true; }

    public Optional<Integer> getProductionOverride() { return productionOverride; }
    public void setProductionOverride(Integer value) {
        this.productionOverride = Optional.ofNullable(value);
    }
    public void clearProductionOverride() { this.productionOverride = Optional.empty(); }

    // Scout support
    public boolean isScoutActive() { return scoutActive; }
    public void setScoutActive(boolean active) { this.scoutActive = active; }
    public void clearScoutActive() { this.scoutActive = false; }
}
