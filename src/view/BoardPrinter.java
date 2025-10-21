package src.view;

import src.model.Player;
import src.model.RegionTile;
import src.model.Principality;
import src.io.interfaces.IOutputService;

public final class BoardPrinter {
    private BoardPrinter(){}

    public static void printPlayerBoard(Player p, IOutputService out){
        Principality board = p.principality();
        int cols = board.width();
        StringBuilder hdr = new StringBuilder("      ");
    for (int c=0;c<cols;c++) hdr.append(pad("Col "+c, 21));
        out.println(hdr.toString());
        out.println("    " + line(cols));

    // Row 0 (far top sites or empty)
        StringBuilder r0a = new StringBuilder(" 0  |");
        StringBuilder r0b = new StringBuilder("    |");
        int leftShiftTop = (board.hasSettlementOuterLeft() ? 2 : (board.hasRoadLeft() ? 1 : 0));
        for (int c=0;c<cols;c++){
            String bld = null;
            // far top sites exist at inner columns (1 and 3) when city is present on that side
            if (board.hasCityLeft() && c == 1 + leftShiftTop) bld = board.getBuildingAt(0,1);
            else if (board.hasCityRight() && c == 3 + leftShiftTop) bld = board.getBuildingAt(0,3);
            r0a.append(cellTop(buildingTitle(bld)));
            r0b.append(cellInfo(buildingInfo(bld)));
        }
        out.println(r0a.toString());
        out.println(r0b.toString());
        out.println("    " + line(cols));

    // Row 1 (top regions)
        StringBuilder r1a = new StringBuilder(" 1  |");
        StringBuilder r1b = new StringBuilder("    |");
        int leftShift = (board.hasSettlementOuterLeft() ? 2 : (board.hasRoadLeft() ? 1 : 0));
        for (int c=0;c<cols;c++){
            String bld = null;
            // outer-left building site sits at col 0 if outer-left settlement exists
            if (board.hasSettlementOuterLeft() && c == 0) bld = board.getBuildingAt(1,0);
            else if (c == 1 + leftShift) bld = board.getBuildingAt(1,1);
            else if (board.hasCityLeft() && c == 2 + leftShift) bld = board.getBuildingAt(1,2);
            else if (c == 3 + leftShift) bld = board.getBuildingAt(1,3);
            else if (board.hasCityRight() && c == 4 + leftShift) bld = board.getBuildingAt(1,4);
            else if (board.hasSettlementOuterRight() && c == cols-1) bld = board.getBuildingAt(1, cols-1);
            if (bld != null) {
                r1a.append(cellTop(buildingTitle(bld)));
                r1b.append(cellInfo(buildingInfo(bld)));
            } else {
                RegionTile t = board.getRegionAt(1,c);
                r1a.append(cellTop(regionTitle(t)));
                r1b.append(cellInfo(regionInfo(t)));
            }
        }
        out.println(r1a.toString());
        out.println(r1b.toString());
        out.println("    " + line(cols));

    // Row 2 (center row): dynamic when left/right edges extend
        StringBuilder r2a = new StringBuilder(" 2  |");
        StringBuilder r2b = new StringBuilder("    |");
        for (int c=0;c<cols;c++){
            String top = "";
            String info = "";
            // Left-side dynamic shift: when only a left road exists, shift = 1; when also an outer-left settlement exists, shift = 2
            int leftShift2 = (board.hasSettlementOuterLeft() ? 2 : (board.hasRoadLeft() ? 1 : 0));
            int leftRoadCol = 0 + leftShift2;                   // left road drawn after any left-side shifts
            int settleLCol  = 1 + leftShift2;                   // inner-left settlement slot
            int centerRoadCol = 2 + leftShift2;
            int settleRCol  = 3 + leftShift2;
            int rightRoadCol = 4 + leftShift2;                  // remains stable even if board extends further
            int outerRightSettleCol = 5 + leftShift2;            // far-right settlement position (when present)
            int outerLeftSettleCol = leftShift2 - 1;             // far-left settlement sits just left of the left road

            if (board.hasSettlementOuterLeft() && outerLeftSettleCol >= 0 && c == outerLeftSettleCol) { top = board.hasCityOuterLeft()?"City":"Settlement"; info = centerInfo(top); }
            else if (c == leftRoadCol) { top = board.hasRoadLeft()?"Road":""; info = centerInfo(top); }
            else if (c == settleLCol) { top = board.hasSettlementLeft()?(board.hasCityLeft()?"City":"Settlement"):""; info = centerInfo(top); }
            else if (c == centerRoadCol) { top = board.hasRoadCenter()?"Road":""; info = centerInfo(top); }
            else if (c == settleRCol) { top = board.hasSettlementRight()?(board.hasCityRight()?"City":"Settlement"):""; info = centerInfo(top); }
            else if (c == rightRoadCol && rightRoadCol < cols) { top = board.hasRoadRight()?"Road":""; info = centerInfo(top); }
            else if (board.hasSettlementOuterRight() && outerRightSettleCol < cols && c == outerRightSettleCol) { top = board.hasCityOuterRight()?"City":"Settlement"; info = centerInfo(top); }
            // Note: outermost columns (0 and cols-1) remain blank as padding beyond edges
            r2a.append(cellTop(centerTitle(top)));
            r2b.append(cellInfo(info));
        }
        out.println(r2a.toString());
        out.println(r2b.toString());
        out.println("    " + line(cols));

    // Row 3 (bottom regions)
        StringBuilder r3a = new StringBuilder(" 3  |");
        StringBuilder r3b = new StringBuilder("    |");
        for (int c=0;c<cols;c++){
            String bld = null;
            if (board.hasSettlementOuterLeft() && c == 0) bld = board.getBuildingAt(3,0);
            else if (c == 1 + leftShift) bld = board.getBuildingAt(3,1);
            else if (board.hasCityLeft() && c == 2 + leftShift) bld = board.getBuildingAt(3,2);
            else if (c == 3 + leftShift) bld = board.getBuildingAt(3,3);
            else if (board.hasCityRight() && c == 4 + leftShift) bld = board.getBuildingAt(3,4);
            else if (board.hasSettlementOuterRight() && c == cols-1) bld = board.getBuildingAt(3, cols-1);
            if (bld != null) {
                r3a.append(cellTop(buildingTitle(bld)));
                r3b.append(cellInfo(buildingInfo(bld)));
            } else {
                RegionTile t = board.getRegionAt(3,c);
                r3a.append(cellTop(regionTitle(t)));
                r3b.append(cellInfo(regionInfo(t)));
            }
        }
        out.println(r3a.toString());
        out.println(r3b.toString());
        out.println("    " + line(cols));

    // Row 4 (far bottom sites or empty)
        StringBuilder r4a = new StringBuilder(" 4  |");
        StringBuilder r4b = new StringBuilder("    |");
        int leftShiftBottom = leftShift; // same calculation
        for (int c=0;c<cols;c++){
            String bld = null;
            if (board.hasCityLeft() && c == 1 + leftShiftBottom) bld = board.getBuildingAt(4,1);
            else if (board.hasCityRight() && c == 3 + leftShiftBottom) bld = board.getBuildingAt(4,3);
            r4a.append(cellTop(buildingTitle(bld)));
            r4b.append(cellInfo(buildingInfo(bld)));
        }
        out.println(r4a.toString());
        out.println(r4b.toString());
        out.println("    " + line(cols));
        out.println("");

    out.println("Storage -> " + p.resourcesSummaryTotal());
    var pts = p.principality().getPoints();
    StringBuilder pointsLine = new StringBuilder();
    pointsLine.append("Points: ")
        .append("VP=").append(pts.getVP())
        .append("  CP=").append(pts.getCP())
        .append("  SP=").append(pts.getSP())
        .append("  FP=").append(pts.getFP())
        .append("  PP=").append(pts.getPP());
    // Advantage badges
    java.util.ArrayList<String> adv = new java.util.ArrayList<>();
    if (p.hasTradeAdvantage()) adv.add("TradeAdv");
    if (p.isStrengthAdvVPAwarded()) adv.add("StrengthAdv");
    if (!adv.isEmpty()) {
        pointsLine.append("  [").append(String.join(" ", adv)).append("]");
    }
    out.println(pointsLine.toString());
        out.println("");
    }

    private static String line(int cols){
        StringBuilder sb = new StringBuilder("+");
        for (int i=0;i<cols;i++) sb.append("-".repeat(21)).append("+");
        return sb.toString();
    }
    private static String pad(String s, int w){
        if (s==null) s="";
        return s + " ".repeat(Math.max(0, w - s.length()));
    }
    private static String cellTop(String s){ return " " + pad(s, 20) + "|"; }
    private static String cellInfo(String s){ return " " + pad(s, 20) + "|"; }

    private static String regionTitle(RegionTile r){
        if (r==null) return "";
        switch (r.getResource()){
            case WOOD:  return "Forest (L):Lumber";
            case BRICK: return "Hill (B):Brick";
            case WHEAT: return "Field (G):Grain";
            case WOOL:  return "Pasture (W):Wool";
            case ORE:   return "Mountain (O):Ore";
            case GOLD:  return "Gold Field (A):Gold";
            default:    return r.getResource().name();
        }
    }
    private static String regionInfo(RegionTile r){
        if (r==null) return "";
        return "d" + r.getDie() + "  " + r.getStored() + "/3";
    }
    private static String centerTitle(String s){
        if (s==null || s.isBlank()) return "";
        return s;
    }
    private static String centerInfo(String s){
        if (s==null || s.isBlank()) return "";
        return "Center";
    }

    private static String buildingTitle(String name){
        if (name == null) return "";
        String n = name.trim().toLowerCase();
        if (isCommonHero(n)) return name + " (Hero)";
        return name; // Abbey, etc.
    }

    private static String buildingInfo(String name){
        if (name == null) return "";
        String n = name.trim().toLowerCase();
        switch (n){
            case "abbey": return "[PP1]";
            case "austin": return "[SP1 FP2]";
            case "candamir": return "[SP4 FP1]";
            case "harald": return "[SP2 FP1]";
            case "inga": return "[SP1 FP3]";
            case "osmund": return "[SP2 FP2]";
            case "siglind": return "[SP2 FP3]";
            default: return "";
        }
    }

    private static boolean isCommonHero(String n){
        return n.equals("austin") || n.equals("candamir") || n.equals("harald") || n.equals("inga") || n.equals("osmund") || n.equals("siglind");
    }
}