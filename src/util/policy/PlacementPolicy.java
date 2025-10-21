package src.util.policy;
import src.model.Principality;
import src.model.BasicCard;
/** Placement validation (base accepts all). */
public interface PlacementPolicy {
    /** null means valid; otherwise return friendly message. */
    String validatePlacement(Principality princ, BasicCard card);
}
