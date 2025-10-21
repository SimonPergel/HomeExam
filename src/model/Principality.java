package src.model;

import java.util.*;
import java.util.EnumMap; 
import java.util.EnumSet;

/**
 * Minimal principality board for the base game, extended with a fixed intro grid for display:
 * - Regions list drives production.
 * - Grid slots (row1/row3) for ASCII rendering.
 * - Settlement/Road flags for centers.
 * - Points holder (VP/CP/SP/FP/PP). getProgressPoints delegates to PP.
 */
public class Principality {
    private final java.util.List<RegionTile> regions = new java.util.ArrayList<>();

    // ASCII grid for intro/base with dynamic width. Start with 5 columns and extend on edge-road build.
    private final java.util.ArrayList<RegionTile> row1 = new java.util.ArrayList<>();
    private final java.util.ArrayList<RegionTile> row3 = new java.util.ArrayList<>();
    private boolean settlementLeft, settlementRight;
    // Track whether a settlement has been upgraded to a city on each inner side
    private boolean cityLeft, cityRight;
    private boolean settlementOuterLeft;  // new: beyond left edge road
    // New outer-edge settlements (beyond edge roads)
    private boolean settlementOuterRight; // for now, implement right side per request
    // Track city upgrades for inner and outer settlements
    private boolean cityOuterLeft, cityOuterRight;
    private boolean roadLeft, roadCenter, roadRight;
    // Building sites (heroes and buildings) adjacent to settlements/cities
    // Inner slots: row1/row3 x col1/col3 (above/below left/right settlement)
    private String buildingTopLeft, buildingBottomLeft, buildingTopRight, buildingBottomRight;
    // Additional building sites unlocked by City upgrade (each city adds +1 above and +1 below)
    // Mapped logically to columns 2 (for left city) and 4 (for right city) in the base 5-wide grid,
    // adjusted by left/right shifts in the printer via display mapping.
    private String buildingTopLeftExtra, buildingBottomLeftExtra;   // col 2 when cityLeft
    private String buildingTopRightExtra, buildingBottomRightExtra; // col 4 when cityRight
    // Outer-edge slots (when outer settlements are built): use col=0 for far-left and col=width-1 for far-right
    private String buildingTopOuterLeft, buildingBottomOuterLeft;
    private String buildingTopOuterRight, buildingBottomOuterRight;
    // Far top/bottom sites (one step further away) unlocked by City at inner columns (1 and 3)
    private String buildingFarTopLeft, buildingFarBottomLeft;     // rows 0 and 4, col 1 (requires cityLeft)
    private String buildingFarTopRight, buildingFarBottomRight;   // rows 0 and 4, col 3 (requires cityRight)

    // Points
    private final Points points = new Points();

    public Principality(){
        // initialize to 5 columns
        for (int i=0;i<5;i++){ row1.add(null); row3.add(null); }
    }

    public void addRegion(RegionTile t){ regions.add(java.util.Objects.requireNonNull(t)); }

    /** Read-only view for effects/tests. */
    public java.util.List<RegionTile> regions(){ return java.util.Collections.unmodifiableList(regions); }

    /** Produce resources for the given die roll. */
    public java.util.Map<Resource,Integer> produce(int die){
        var m = new java.util.EnumMap<Resource,Integer>(Resource.class);
        for (var r : Resource.values()) m.put(r, 0);
        for (var t : regions) if (t.getDie()==die) m.put(t.getResource(), m.get(t.getResource())+1);
        return m;
    }

    // ---- Points / Progress ----
    public Points getPoints(){ return points; }
    public int getProgressPoints(){ return points.getPP(); }
    public void addProgressPoints(int delta){ points.addPP(delta); }

    // ---- Grid API (intro display) ----
    public int width(){ return row1.size(); }

    private void checkCol(int c){ if (c<0 || c>=width()) throw new IllegalArgumentException("col 0.."+(width()-1)); }

    public void placeRegionTop(int col, RegionTile t){ checkCol(col); row1.set(col, t); addRegion(t); }
    public void placeRegionBottom(int col, RegionTile t){ checkCol(col); row3.set(col, t); addRegion(t); }
    public RegionTile getTopRegion(int col){ checkCol(col); return row1.get(col); }
    public RegionTile getBottomRegion(int col){ checkCol(col); return row3.get(col); }

    public void placeSettlementLeft(){ settlementLeft = true; }
    public void placeSettlementRight(){ settlementRight = true; }
    public void placeRoadLeft(){ roadLeft = true; }
    public void placeRoadCenter(){ roadCenter = true; }
    public void placeRoadRight(){ roadRight = true; }

    public boolean hasSettlementLeft(){ return settlementLeft; }
    public boolean hasSettlementRight(){ return settlementRight; }
    public boolean hasCityLeft(){ return cityLeft; }
    public boolean hasCityRight(){ return cityRight; }
    public boolean hasSettlementOuterLeft(){ return settlementOuterLeft; }
    public boolean hasSettlementOuterRight(){ return settlementOuterRight; }
    // Outer city getters
    public boolean hasCityOuterLeft(){ return cityOuterLeft; }
    public boolean hasCityOuterRight(){ return cityOuterRight; }
    public boolean hasRoadLeft(){ return roadLeft; }
    public boolean hasRoadCenter(){ return roadCenter; }
    public boolean hasRoadRight(){ return roadRight; }

    // ---- Building site API (used by heroes and buildings like Abbey) ----
    /** True if a building can be placed at the given site: row must be 0, 1, 3, or 4; allowed columns:
     *  - 0 (outer-left) if an outer-left settlement exists
     *  - 1 (inner-left) if a left settlement/city exists
     *  - 2 (extra-left) if a LEFT CITY exists (unlocked by upgrade)
     *  - 3 (inner-right) if a right settlement/city exists
     *  - 4 (extra-right) if a RIGHT CITY exists (unlocked by upgrade)
     *  - width()-1 (outer-right) if an outer-right settlement exists
     *  The target site must be empty. */
    public boolean canPlaceBuildingAt(int row, int col){
        if (!(row == 0 || row == 1 || row == 3 || row == 4)) return false;
        if (!(col == 0 || col == 1 || col == 2 || col == 3 || col == 4 || col == width()-1)) return false;
        // Require adjacent settlement/city (or city for extra sites)
        if (col == 0 && !settlementOuterLeft) return false;
        if (col == 1) {
            // inner-left: allowed for rows 1/3 with settlement/city; rows 0/4 only if city
            if (row == 0 || row == 4) { if (!cityLeft) return false; }
            else if (!(settlementLeft || cityLeft)) return false;
        }
        if (col == 2 && !cityLeft) return false;     // extra site only when city on left
        if (col == 3) {
            if (row == 0 || row == 4) { if (!cityRight) return false; }
            else if (!(settlementRight || cityRight)) return false;
        }
        if (col == 4 && !cityRight) return false;    // extra site only when city on right
        if (col == width()-1 && !settlementOuterRight) return false;
        // Slot must be empty
        return getBuildingAt(row, col) == null;
    }

    /** Place a building/hero name on the given building site. */
    public void placeBuildingAt(int row, int col, String name){
        if (!canPlaceBuildingAt(row, col))
            throw new IllegalStateException("illegal building placement at ("+row+","+col+")");
        if (row == 0 && col == 1) buildingFarTopLeft = name;
        else if (row == 4 && col == 1) buildingFarBottomLeft = name;
        else if (row == 1 && col == 1) buildingTopLeft = name;
        else if (row == 3 && col == 1) buildingBottomLeft = name;
        else if (row == 1 && col == 2) buildingTopLeftExtra = name;
        else if (row == 3 && col == 2) buildingBottomLeftExtra = name;
        else if (row == 0 && col == 3) buildingFarTopRight = name;
        else if (row == 4 && col == 3) buildingFarBottomRight = name;
        else if (row == 1 && col == 3) buildingTopRight = name;
        else if (row == 3 && col == 3) buildingBottomRight = name;
        else if (row == 1 && col == 4) buildingTopRightExtra = name;
        else if (row == 3 && col == 4) buildingBottomRightExtra = name;
        else if (row == 1 && col == 0) buildingTopOuterLeft = name;
        else if (row == 3 && col == 0) buildingBottomOuterLeft = name;
        else if (row == 1 && col == width()-1) buildingTopOuterRight = name;
        else if (row == 3 && col == width()-1) buildingBottomOuterRight = name;
    }

    /** Returns building/hero name at site or null. */
    public String getBuildingAt(int row, int col){
        if (row == 0 && col == 1) return buildingFarTopLeft;
        if (row == 4 && col == 1) return buildingFarBottomLeft;
        if (row == 1 && col == 1) return buildingTopLeft;
        if (row == 3 && col == 1) return buildingBottomLeft;
        if (row == 1 && col == 2) return buildingTopLeftExtra;
        if (row == 3 && col == 2) return buildingBottomLeftExtra;
        if (row == 0 && col == 3) return buildingFarTopRight;
        if (row == 4 && col == 3) return buildingFarBottomRight;
        if (row == 1 && col == 3) return buildingTopRight;
        if (row == 3 && col == 3) return buildingBottomRight;
        if (row == 1 && col == 4) return buildingTopRightExtra;
        if (row == 3 && col == 4) return buildingBottomRightExtra;
        if (row == 1 && col == 0) return buildingTopOuterLeft;
        if (row == 3 && col == 0) return buildingBottomOuterLeft;
        if (row == 1 && col == width()-1) return buildingTopOuterRight;
        if (row == 3 && col == width()-1) return buildingBottomOuterRight;
        return null;
    }

    /** Clear a building from a site (no point side-effects). Returns the removed name or null. */
    public String clearBuildingAt(int row, int col){
        String prev = getBuildingAt(row, col);
        if (row == 0 && col == 1) buildingFarTopLeft = null;
        else if (row == 4 && col == 1) buildingFarBottomLeft = null;
        else if (row == 1 && col == 1) buildingTopLeft = null;
        else if (row == 3 && col == 1) buildingBottomLeft = null;
        else if (row == 1 && col == 2) buildingTopLeftExtra = null;
        else if (row == 3 && col == 2) buildingBottomLeftExtra = null;
        else if (row == 0 && col == 3) buildingFarTopRight = null;
        else if (row == 4 && col == 3) buildingFarBottomRight = null;
        else if (row == 1 && col == 3) buildingTopRight = null;
        else if (row == 3 && col == 3) buildingBottomRight = null;
        else if (row == 1 && col == 4) buildingTopRightExtra = null;
        else if (row == 3 && col == 4) buildingBottomRightExtra = null;
        else if (row == 1 && col == 0) buildingTopOuterLeft = null;
        else if (row == 3 && col == 0) buildingBottomOuterLeft = null;
        else if (row == 1 && col == width()-1) buildingTopOuterRight = null;
        else if (row == 3 && col == width()-1) buildingBottomOuterRight = null;
        return prev;
    }

    /** Count all buildings currently placed (heroes and buildings). */
    public int countBuildings(){
        int n = 0;
        if (buildingFarTopLeft != null) n++;
        if (buildingFarBottomLeft != null) n++;
        if (buildingTopLeft != null) n++;
        if (buildingBottomLeft != null) n++;
        if (buildingTopLeftExtra != null) n++;
        if (buildingBottomLeftExtra != null) n++;
        if (buildingFarTopRight != null) n++;
        if (buildingFarBottomRight != null) n++;
        if (buildingTopRight != null) n++;
        if (buildingBottomRight != null) n++;
        if (buildingTopRightExtra != null) n++;
        if (buildingBottomRightExtra != null) n++;
        if (buildingTopOuterLeft != null) n++;
        if (buildingBottomOuterLeft != null) n++;
        if (buildingTopOuterRight != null) n++;
        if (buildingBottomOuterRight != null) n++;
        return n;
    }

    /** List of occupied building site coordinates: entries are {row, col}. */
    public java.util.List<int[]> getOccupiedBuildingSites(){
        java.util.ArrayList<int[]> list = new java.util.ArrayList<>();
        if (buildingFarTopLeft != null) list.add(new int[]{0,1});
        if (buildingTopLeft != null) list.add(new int[]{1,1});
        if (buildingBottomLeft != null) list.add(new int[]{3,1});
        if (buildingFarBottomLeft != null) list.add(new int[]{4,1});
        if (buildingTopLeftExtra != null) list.add(new int[]{1,2});
        if (buildingBottomLeftExtra != null) list.add(new int[]{3,2});
        if (buildingFarTopRight != null) list.add(new int[]{0,3});
        if (buildingTopRight != null) list.add(new int[]{1,3});
        if (buildingBottomRight != null) list.add(new int[]{3,3});
        if (buildingFarBottomRight != null) list.add(new int[]{4,3});
        if (buildingTopRightExtra != null) list.add(new int[]{1,4});
        if (buildingBottomRightExtra != null) list.add(new int[]{3,4});
        if (buildingTopOuterLeft != null) list.add(new int[]{1,0});
        if (buildingBottomOuterLeft != null) list.add(new int[]{3,0});
        if (buildingTopOuterRight != null) list.add(new int[]{1,width()-1});
        if (buildingBottomOuterRight != null) list.add(new int[]{3,width()-1});
        return list;
    }

    /** True if any building site currently holds a card with the given name (case-insensitive). */
    public boolean hasBuildingNamed(String name){
        if (name == null) return false;
        String target = name.trim();
        return (buildingTopLeft != null && buildingTopLeft.equalsIgnoreCase(target))
            || (buildingFarTopLeft != null && buildingFarTopLeft.equalsIgnoreCase(target))
            || (buildingFarBottomLeft != null && buildingFarBottomLeft.equalsIgnoreCase(target))
            || (buildingBottomLeft != null && buildingBottomLeft.equalsIgnoreCase(target))
            || (buildingTopLeftExtra != null && buildingTopLeftExtra.equalsIgnoreCase(target))
            || (buildingBottomLeftExtra != null && buildingBottomLeftExtra.equalsIgnoreCase(target))
            || (buildingFarTopRight != null && buildingFarTopRight.equalsIgnoreCase(target))
            || (buildingFarBottomRight != null && buildingFarBottomRight.equalsIgnoreCase(target))
            || (buildingTopRight != null && buildingTopRight.equalsIgnoreCase(target))
            || (buildingBottomRight != null && buildingBottomRight.equalsIgnoreCase(target))
            || (buildingTopRightExtra != null && buildingTopRightExtra.equalsIgnoreCase(target))
            || (buildingBottomRightExtra != null && buildingBottomRightExtra.equalsIgnoreCase(target))
            || (buildingTopOuterLeft != null && buildingTopOuterLeft.equalsIgnoreCase(target))
            || (buildingBottomOuterLeft != null && buildingBottomOuterLeft.equalsIgnoreCase(target))
            || (buildingTopOuterRight != null && buildingTopOuterRight.equalsIgnoreCase(target))
            || (buildingBottomOuterRight != null && buildingBottomOuterRight.equalsIgnoreCase(target));
    }

    /** Rule convenience: Parish Hall reduces Exchange search cost to 1. */
    public boolean hasParishHall(){
        return hasBuildingNamed("Parish Hall");
    }

    // ---- Display-column helpers for building sites ----
    /** Compute left-shift used by BoardPrinter for inner slots when left side is extended. */
    private int computeLeftShift(){
        return settlementOuterLeft ? 2 : (roadLeft ? 1 : 0);
    }

    /** Map a displayed column index (as the header shows) to the logical building site column.
     * Returns -1 if the display column is not a valid building site location. */
    public int mapDisplayColToBuildingSiteCol(int displayCol){
        int cols = width();
        int leftShift = computeLeftShift();
        // Outer-left site
        if (settlementOuterLeft && displayCol == 0) return 0;
        // Inner-left site
        if (displayCol == 1 + leftShift) return 1;
        // Extra-left site (only when cityLeft)
        if (cityLeft && displayCol == 2 + leftShift) return 2;
        // Inner-right site
        if (displayCol == 3 + leftShift) return 3;
        // Extra-right site (only when cityRight)
        if (cityRight && displayCol == 4 + leftShift) return 4;
        // Outer-right site
        if (settlementOuterRight && displayCol == cols - 1) return cols - 1;
        return -1;
    }

    /** Map a displayed column to a settlement/city column slot (supports inner and outer).
     * Returns one of: 0 (outer-left), 1 (inner-left), 3 (inner-right), width()-1 (outer-right), or -1 if none. */
    public int mapDisplayColToAnySettlementCol(int displayCol){
        int cols = width();
        int leftShift2 = computeLeftShift();
        int settleLColDisp  = 1 + leftShift2;
        int settleRColDisp  = 3 + leftShift2;
        int outerLeftSettleCol = leftShift2 - 1;
        int outerRightSettleCol = 5 + leftShift2;
        if (displayCol == settleLColDisp) return 1;
        if (displayCol == settleRColDisp) return 3;
        if (settlementOuterLeft && outerLeftSettleCol >= 0 && displayCol == outerLeftSettleCol) return 0;
        if (settlementOuterRight && outerRightSettleCol < cols && displayCol == outerRightSettleCol) return cols - 1; // canonicalize to last col
        return -1;
    }

    /** True if a building can be placed at row and the printed display column number (see board header). */
    public boolean canPlaceBuildingAtDisplay(int row, int displayCol){
        int siteCol = mapDisplayColToBuildingSiteCol(displayCol);
        if (siteCol < 0) return false;
        return canPlaceBuildingAt(row, siteCol);
    }

    /** Place building using a printed display column number (see board header). */
    public void placeBuildingAtDisplay(int row, int displayCol, String name){
        int siteCol = mapDisplayColToBuildingSiteCol(displayCol);
        if (siteCol < 0) throw new IllegalStateException("No building site at display column " + displayCol);
        placeBuildingAt(row, siteCol, name);
    }

    /** Map a displayed column index to the logical center settlement column (1 for left, 3 for right).
     *  Returns -1 if the display column does not correspond to a center settlement slot. */
    public int mapDisplayColToCenterSettlementCol(int displayCol){
        int leftShift = computeLeftShift();
        int settleLCol  = 1 + leftShift;
        int settleRCol  = 3 + leftShift;
        if (displayCol == settleLCol) return 1;
        if (displayCol == settleRCol) return 3;
        return -1;
    }

    // ---- Backward-compat wrappers for hero-specific API ----
    public boolean canPlaceHeroAt(int row, int col){ return canPlaceBuildingAt(row, col); }
    public void placeHeroAt(int row, int col, String name){ placeBuildingAt(row, col, name); }
    public String getHeroAt(int row, int col){ return getBuildingAt(row, col); }

    /** Swap die numbers of two regions (Relocation). Indices refer to the linear list. */
    public void swapRegionDice(int i, int j){
        if (i<0 || i>=regions.size() || j<0 || j>=regions.size())
            throw new IllegalArgumentException("index out of range");
        int di = regions.get(i).getDie();
        int dj = regions.get(j).getDie();
        regions.get(i).setDie(dj);
        regions.get(j).setDie(di);
    }

    ////////////// --- Trade-ship state (2:1) --- //////////////
    private final java.util.EnumMap<Resource, Integer> tradeRatios =
            new java.util.EnumMap<>(Resource.class);
    private final java.util.EnumSet<Resource> tradeShips =
            java.util.EnumSet.noneOf(Resource.class);

    public void grantTradeShip(Resource r) {
        tradeShips.add(r);
        tradeRatios.put(r, 2); // specialized 2:1 for that resource
    }

    public boolean hasAnyTradeShip() { return !tradeShips.isEmpty(); }

    public int getTradeRatio(Resource r) {
        // 2 if we have a ship for r, else default 3 (base game)
        return tradeRatios.getOrDefault(r, 3);
    }
    // Do we have a ship for a specific resource?
    public boolean hasTradeShip(Resource r) {
        return tradeShips.contains(r);
    }

    // Expose a read-only view if you ever want to render or save this
    public java.util.Set<Resource> getTradeShips() {
        return java.util.Collections.unmodifiableSet(tradeShips);
    }
    // ===== Coordinate system helpers (5x5 board, legacy-compatible) =====
    // rows: 0..4 (display)
    //   1 = top regions row
    //   2 = center row (settlement/road/settlement)
    //   3 = bottom regions row
    // cols: 0..4; center row logical slots are {1 (settlementL), 2 (road), 3 (settlementR)}

    private void checkRowCol(int row, int col) {
        if (row < 0 || row > 4) throw new IllegalArgumentException("row must be 0..4");
        if (col < 0 || col >= width()) throw new IllegalArgumentException("col must be 0.." + (width()-1));
        // Do not restrict center row here so non-placement helpers (like getRegionAt) can probe safely.
    }

    /** What kind of cell is at (row,col)? "regionTop", "regionBottom", "settlementL", "settlementR", "roadLeft", "roadCenter", "roadRight", "invalid". */
    public String cellKind(int row, int col) {
        if (row == 1) return "regionTop";
        if (row == 3) return "regionBottom";
        if (row == 2) {
            if (col == 0) return "roadLeft"; // edge road slot (extension)
            if (col == 1) return "settlementL";
            if (col == 2) return "roadCenter";
            if (col == 3) return "settlementR";
            if (col == 4) return "roadRight"; // edge road slot (extension)
        }
        return "invalid";
    }

    /** Returns true if the slot is empty (for its allowed type). */
    public boolean isEmptyAt(int row, int col) {
        checkRowCol(row, col);
        if (row == 1) return row1.get(col) == null;
        if (row == 3) return row3.get(col) == null;
        // row 2
        if (row == 2) {
            if (col == 0) return !roadLeft;
            if (col == 1) return !settlementLeft;
            if (col == 2) return !roadCenter;
            if (col == 3) return !settlementRight;
            if (col == 4) return !roadRight;
            // dynamic far-right settlement (last column) when right edge is extended
            if (col == width()-1) return !settlementOuterRight;
            return false;
        }
        return false;
    }

    /** Region accessor by coordinate; returns null if not a region cell or empty. */
    public RegionTile getRegionAt(int row, int col) {
        checkRowCol(row, col);
        if (row == 1) return row1.get(col);
        if (row == 3) return row3.get(col);
        return null;
    }

    /** True if a region could be placed at (row,col) under the simplified board rules.
     *  Rules: row must be 1 (top) or 3 (bottom); col must be 0,2, or 4; slot must be empty.
     */
    public boolean canPlaceRegionAt(int row, int col) {
        checkRowCol(row, col);
        if (!(row == 1 || row == 3)) return false;
        // Allow any column within current width; starter UI suggests 0,2,4, but after extension indices shift.
        if (col < 0 || col >= width()) return false;
        return isEmptyAt(row, col);
    }

    /** Place a region on either top (row 1) or bottom (row 3). Also updates production regions list. */
    public void placeRegionAt(int row, int col, RegionTile t) {
        Objects.requireNonNull(t, "region");
        if (!canPlaceRegionAt(row, col)) throw new IllegalStateException("illegal region placement at ("+row+","+col+")");

        if (row == 1) { row1.set(col, t); }
        else if (row == 3) { row3.set(col, t); }
        addRegion(t); // keep regions list in sync for production
    }

    /** True if a road could be placed on row 2 at allowed columns (0,2,4) and legal per current state.
     *  Rules:
     *  - Roads only on row 2.
     *  - (2,0) is the left edge road; may be built to extend before placing a left settlement; slot must be empty.
     *  - (2,2) is the center road; only one allowed.
     *  - (2,4) is the right edge road; may be built to extend before placing a right settlement; slot must be empty.
     */
    public boolean canPlaceRoadAt(int row, int col) {
        checkRowCol(row, col);
        if (row != 2) return false;
        // Edge/center columns are based on current width after extensions:
        // Pattern: [0]=padding, [1]=maybe roadLeft, [2]=settlementLeft, [3]=roadCenter, [4]=settlementRight, [last-1]=maybe roadRight, [last]=padding
        // Incoming requests will still be 0/2/4 from prompts; we normalize here.
        if (col == 0) return !roadLeft;                              // left edge request
        if (col == 2) return !roadCenter;                            // center
        if (col == 4) return !roadRight;                             // right edge request
        return false;
    }

    /** True if a settlement could be placed at the left/right center slot.
     *  Board-level check is minimal: row=2, slot empty. Adjacency requirements for
     *  building new settlements are enforced by the placement handler, so that
     *  setup code can seed starting settlements without edge roads.
     */
    public boolean canPlaceSettlementAt(int row, int col) {
        checkRowCol(row, col);
        if (row != 2) return false;
        // inner slots
        if (col == 1) return !settlementLeft;
        if (col == 3) return !settlementRight;
        // outer-left: allowed at first column only if we have a left edge road and no far-left settlement yet
        if (col == 0) return hasRoadLeft() && !settlementOuterLeft;
        // outer-right: allowed at last column only if we have a right edge road and no far-right settlement yet
        if (col == width()-1) return hasRoadRight() && !settlementOuterRight;
        return false;
    }

    /** True if a city upgrade can be placed on an existing settlement (row 2; same slot). */
    public boolean canPlaceCityAt(int row, int col) {
        checkRowCol(row, col);
        if (row != 2) return false;
        // Inner left/right
        if (col == 1) return settlementLeft && !cityLeft;
        if (col == 3) return settlementRight && !cityRight;
        // Outer left/right
        if (col == 0) return settlementOuterLeft && !cityOuterLeft;
        if (col == width()-1) return settlementOuterRight && !cityOuterRight;
        return false;
    }

    /** Place a road at row 2 in columns 0,2,4 depending on adjacency rules. */
    public void placeRoadAt(int row, int col) {
        if (!canPlaceRoadAt(row, col)) throw new IllegalStateException("illegal road placement at ("+row+","+col+")");
        // Extend board when building at an edge to create visual column for the road and keep spacing
        if (col == 0) {
            extendLeft();
            roadLeft = true;
        } else if (col == 2) {
            roadCenter = true;
        } else if (col == 4) {
            extendRight();
            roadRight = true;
        }
    }

    private void extendLeft(){
        // Insert padding column at index 0 for top/bottom rows
        row1.add(0, null);
        row3.add(0, null);
    }

    private void extendRight(){
        row1.add(null);
        row3.add(null);
    }

    /** Place a settlement at left/right center. */
    public void placeSettlementAt(int row, int col) {
        if (!canPlaceSettlementAt(row, col)) throw new IllegalStateException("illegal settlement placement at ("+row+","+col+")");
        if (col == 1) {
            settlementLeft = true;
        } else if (col == 3) {
            settlementRight = true;
        } else if (col == 0) {
            // outer-left settlement at current first column; then extend to keep a blank beyond on the far left
            settlementOuterLeft = true;
            extendLeft();
        } else if (col == width()-1) {
            // outer-right settlement at current last column
            settlementOuterRight = true;
            // After placing, extend board to keep a blank column beyond for future growth
            extendRight();
        }
    }

    /** Upgrade an existing settlement to a city (toggle/track as needed). */
    public void upgradeCityAt(int row, int col) {
        if (!canPlaceCityAt(row, col)) throw new IllegalStateException("illegal city placement at ("+row+","+col+")");
        if (col == 1) {
            cityLeft = true;   // unlock extra building sites at col 2 (top/bottom)
            // settlementLeft remains true to indicate a center occupied by city; keep for compatibility
        } else if (col == 3) {
            cityRight = true;  // unlock extra building sites at col 4 (top/bottom)
            // settlementRight remains true for compatibility
        } else if (col == 0) {
            cityOuterLeft = true;  // outer city (no extra inner sites unlocked)
        } else if (col == width()-1) {
            cityOuterRight = true; // outer city (no extra inner sites unlocked)
        }
    }
}
