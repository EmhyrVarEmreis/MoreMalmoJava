package xyz.morecraft.dev.malmo.util;

public class WayUtils {

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

}