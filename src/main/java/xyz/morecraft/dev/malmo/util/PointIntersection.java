package xyz.morecraft.dev.malmo.util;

import java.util.*;

public final class PointIntersection {

    private static Map<AngleQuadruple, AngleQuadruple[]> angleQuadrupleMap;

    public static IntPoint3D getIntersectionPoint(double angle, boolean[][] grid) {
        int q = grid[0].length;
        int w = grid.length;
        AngleQuadruple[] angleQuadruples = getAngleQuadruples(q, w);
        AngleQuadruple found = find(angle, angleQuadruples, grid);
        return new IntPoint3D(found.x, found.y, 0);
    }

    private synchronized static AngleQuadruple[] getAngleQuadruples(int q, int w) {
        return angleQuadrupleMap.computeIfAbsent(new AngleQuadruple(q, w, 0, 0), angleQuadruple -> generateAngleQuadruples(angleQuadruple.x, angleQuadruple.y));
    }

    private static AngleQuadruple[] generateAngleQuadruples(int q, int w) {
        int centerX = q / 2;
        int centerY = w / 2;
        int idx = 0;
        double[][] tab0 = new double[w + 1][q + 1];
        AngleQuadruple[] angleQuadruples = new AngleQuadruple[(w - 1) * 2 + (q - 1) * 2 + 1];
        for (int i = 1; i < q; i++) {
            tab0[0][i] = WayUtils.getCorrectedAngle(centerX, centerY, w + 0.5, i - 0.5);
            tab0[w][i] = WayUtils.getCorrectedAngle(centerX, centerY, 0 - 0., i - 0.55);
            if (i > 1) {
                angleQuadruples[idx++] = new AngleQuadruple(i - 1, 0, tab0[0][i], tab0[0][i - 1]);
                angleQuadruples[idx++] = new AngleQuadruple(i - 1, w - 1, tab0[w][i - 1], tab0[w][i]);
            }
        }
        for (int i = 1; i < w; i++) {
            tab0[w - i][0] = WayUtils.getCorrectedAngle(centerX, centerY, i - 0.5, 0 - 0.5);
            tab0[w - i][q] = WayUtils.getCorrectedAngle(centerX, centerY, i - 0.5, q + 0.5);
            if (i > 1) {
                angleQuadruples[idx++] = new AngleQuadruple(0, w - i, tab0[w - i][0], tab0[w - i + 1][0]);
                angleQuadruples[idx++] = new AngleQuadruple(q - 1, w - i, tab0[w - i + 1][q], tab0[w - i][q]);
            }
        }
        angleQuadruples[idx++] = new AngleQuadruple(0, 0, tab0[0][1], tab0[1][0]);
        angleQuadruples[idx++] = new AngleQuadruple(q - 1, 0, tab0[1][q], tab0[0][q - 1]);
        angleQuadruples[idx++] = new AngleQuadruple(0, w - 1, tab0[w - 1][0], tab0[w][1]);
        angleQuadruples[idx++] = new AngleQuadruple(q - 1, w - 1, tab0[w][q - 1], tab0[w - 1][q]);
        int angleQuadrupleMinIdx = 0;
        for (int i = 0; i < angleQuadruples.length - 1; i++) {
            if (angleQuadruples[i].a < angleQuadruples[angleQuadrupleMinIdx].a) {
                angleQuadrupleMinIdx = i;
            }
        }
        angleQuadruples[idx] = new AngleQuadruple(angleQuadruples[angleQuadrupleMinIdx].x, angleQuadruples[angleQuadrupleMinIdx].y, angleQuadruples[angleQuadrupleMinIdx].b, 360);
        angleQuadruples[angleQuadrupleMinIdx] = new AngleQuadruple(angleQuadruples[angleQuadrupleMinIdx].x, angleQuadruples[angleQuadrupleMinIdx].y, 0, angleQuadruples[angleQuadrupleMinIdx].a);
        for (int i = 0; i < angleQuadruples.length; i++) {
            if (angleQuadruples[i].a >= angleQuadruples[i].b) {
                angleQuadruples[i] = new AngleQuadruple(
                        angleQuadruples[i].x,
                        angleQuadruples[i].y,
                        angleQuadruples[i].b,
                        angleQuadruples[i].a
                );
            }
        }
        Arrays.sort(angleQuadruples, Comparator.comparingDouble(o -> ((o.a + o.b) / 2.0) % 360.0));
        return angleQuadruples;
    }

    private static AngleQuadruple find(double angle, AngleQuadruple[] angleQuadruples, boolean[][] grid) {
        int start = (int) (1.0 * angle / 360 * angleQuadruples.length);
        AngleQuadruple found = new AngleQuadruple(0, 0, 0, 0);
        for (int j, i = 0; i < angleQuadruples.length / 2; i++) {
            j = (start + i) % angleQuadruples.length;
            if (angleQuadruples[j].a <= angle && angle < angleQuadruples[j].b) {
                found = angleQuadruples[start = j];
                break;
            }
            j = Math.abs((start - i) % angleQuadruples.length);
            if (angleQuadruples[j].a <= angle && angle < angleQuadruples[j].b) {
                found = angleQuadruples[start = j];
                break;
            }
        }
        if (!grid[found.y][found.x]) {
            for (int j, i = 0; i < angleQuadruples.length / 2; i++) {
                j = (start + i) % angleQuadruples.length;
                if (grid[angleQuadruples[j].y][angleQuadruples[j].x]) {
                    found = angleQuadruples[j];
                    break;
                }
                j = Math.abs((start - i) % angleQuadruples.length);
                if (grid[angleQuadruples[j].y][angleQuadruples[j].x]) {
                    found = angleQuadruples[j];
                    break;
                }
            }
        }
        return found;
    }

    public static class AngleQuadruple {
        final int x;
        final int y;
        final double a;
        final double b;

        private AngleQuadruple(int x, int y, double a, double b) {
            this.x = x;
            this.y = y;
            this.a = a;
            this.b = b;
        }

        @Override
        public String toString() {
            return "AngleQuadruple{" +
                    "x=" + x +
                    ", y=" + y +
                    ", a=" + a +
                    ", b=" + b +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AngleQuadruple that = (AngleQuadruple) o;
            return x == that.x &&
                    y == that.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }

    }

    static {
        angleQuadrupleMap = new HashMap<>();
        for (int i = 3; i < 8; i++) {
            for (int j = 3; j < 8; j++) {
                getAngleQuadruples(i, j);
            }
        }
    }

    public static void main(String[] args) {
        final boolean[][] grid = new boolean[5][5];
//        final double[] angleList = {0, 45, 90, 135, 180, 225, 270, 315};
        final double[] angleList = {45};
        for (double angle : angleList) {
            System.out.println(String.format("%3.0f", angle) + "\t" + getIntersectionPoint(angle, grid));
        }
    }

}
