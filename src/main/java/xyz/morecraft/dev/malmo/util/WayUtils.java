package xyz.morecraft.dev.malmo.util;

import java.util.Collection;

import static xyz.morecraft.dev.malmo.util.CardinalDirection.*;

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

    public static CardinalDirection someAlgorithm(final boolean[][] grid, final int goalX, final int goalY) {
        return someAlgorithm(grid, grid[0].length / 2, grid.length / 2, goalX, goalY);
    }

    // x/y starting from 0
    private static CardinalDirection someAlgorithm(final boolean[][] grid, final int x, final int y, final int goalX, final int goalY) {
        for (int i = Math.min(y - 1, grid.length); i < Math.min((y + 1), grid.length); i++) {
            for (int j = Math.min(x - 1, grid[0].length); j < Math.min((x + 1), grid[0].length); j++) {
                if (grid[i][j]) {

                }
            }
        }
        return N;
    }

    private static Collection<CardinalDirection> someAlgorithm(final boolean[][] grid, final int x, final int y, final int goalX, final int goalY, final Collection<CardinalDirection> trace) {

        return trace;
    }

}
