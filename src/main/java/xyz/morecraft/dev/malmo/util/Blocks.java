package xyz.morecraft.dev.malmo.util;

@SuppressWarnings("WeakerAccess")
public class Blocks {

    public static String BLOCK_AIR = "air";
    public static String BLOCK_DIRT = "dirt";
    public static String BLOCK_GLOWSTONE = "glowstone";
    public static String BLOCK_GRASS = "grass";
    public static String BLOCK_STONE = "stone";

    public static boolean isWalkable(String blockName){
        return !BLOCK_DIRT.equalsIgnoreCase(blockName);
    }

}
