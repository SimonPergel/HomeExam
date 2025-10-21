package src.util;

import src.model.Resource;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

public final class CostParser {
  private CostParser(){}

/** Parses compact cost codes. Canonical mapping required by spec:
 *  L=Lumber, B=Brick, O=Ore, G=Grain, W=Wool, A=Gold.
 *  Legacy compatibility: accept H as Grain (Wheat) from older data.
 */
  public static Map<Resource,Integer> parse(String code){
    Map<Resource,Integer> m = new EnumMap<>(Resource.class);
    if (code == null || code.isBlank()) return m;

    for (char c : code.trim().toUpperCase(Locale.ROOT).toCharArray()){
      Resource r;
      switch (c){
        case 'L': r = Resource.WOOD;  break; // Lumber
        case 'W': r = Resource.WOOL;  break; // Wool
        case 'B': r = Resource.BRICK; break;
        case 'O': r = Resource.ORE;   break; // Ore
        case 'A': r = Resource.GOLD;  break; // Gold (one-syllable letter per spec)
        case 'G': r = Resource.WHEAT; break; // Grain
        case 'H': r = Resource.WHEAT; break; // Legacy: H was Wheat/Grain
        default : r = null;
      }
      if (r != null) inc(m, r); 
    }
    return m;
  }

  private static void inc(Map<Resource,Integer> m, Resource r){
    m.put(r, m.getOrDefault(r, 0) + 1);
  }

  /** Convert legacy cost codes to the new display letters without changing order:
   *   H->G (Grain). Other letters unchanged. Null-safe. */
  public static String legacyToNewLetters(String code){
    if (code == null || code.isBlank()) return code;
    StringBuilder sb = new StringBuilder(code.length());
    for (char ch : code.toCharArray()){
      char u = Character.toUpperCase(ch);
      if (u == 'H') sb.append('G');
      else sb.append(u);
    }
    return sb.toString();
  }
}