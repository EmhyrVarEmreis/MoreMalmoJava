package xyz.morecraft.dev.malmo.util;

import java.util.LinkedHashMap;
import java.util.Map;

import static xyz.morecraft.dev.malmo.util.CardinalDirection.*;

public class WayUtils {

    public final static Map<CardinalDirection, int[]> CARDINAL_DIRECTION_TRANSLATE_MAP;

    public static String[][] revertGrid(String[][] oldGrid, int r) {
        final String[][] newGrid = new String[r][r];
        for (int i = 0; i < r; i++) {
            for (int j = 0; j < r; j++) {
                newGrid[i][j] = oldGrid[r - i - 1][r - j - 1];
            }
        }
        return newGrid;
    }

    public static double getAngle(double x1, double z1, double x2, double z2) {
        double angle = Math.toDegrees(Math.atan2(z2 - z1, x2 - x1));
        if (angle < 0) {
            angle += 360;
        }
        return angle;
    }

    public static double getCorrectedAngle(double x1, double z1, double x2, double z2) {
        return (getAngle(x1, z1, x2, z2) + 90) % 360;
    }

    public static double getSymmetricAngle(double refAngle, double currAngle) {
        return 180 - Math.abs((currAngle + refAngle) % 360 - 180);
    }

    public static CardinalDirection getOppositeSimpleDimension(final CardinalDirection dim) {
        if (dim == N) {
            return S;
        } else if (dim == E) {
            return W;
        } else if (dim == S) {
            return N;
        } else if (dim == W) {
            return E;
        } else {
            return dim;
        }
    }

    static {
        CARDINAL_DIRECTION_TRANSLATE_MAP = new LinkedHashMap<>();
        CARDINAL_DIRECTION_TRANSLATE_MAP.put(S, new int[]{0, -1});
        CARDINAL_DIRECTION_TRANSLATE_MAP.put(W, new int[]{1, 0});
        CARDINAL_DIRECTION_TRANSLATE_MAP.put(N, new int[]{0, 1});
        CARDINAL_DIRECTION_TRANSLATE_MAP.put(E, new int[]{-1, 0});
    }

}
