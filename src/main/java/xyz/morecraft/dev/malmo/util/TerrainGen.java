package xyz.morecraft.dev.malmo.util;

import com.microsoft.msr.malmo.MissionSpec;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Random;
import java.util.function.BiConsumer;

public class TerrainGen {

    public static Random generator = new Random();

    public static Pair<IntPoint3D, IntPoint3D> emptyRoomWithTransverseObstacles(MissionSpec spec, int xL, int zL, int stepDistance, String block, int yOffset) {
        final int h = 80;
        final int hMax = 90;
        final IntPoint3D p1 = new IntPoint3D((int) Math.round(xL / 2.0), h, 0);
        final IntPoint3D p2 = new IntPoint3D((int) Math.round(xL / 2.0), h, zL - 1);
        spec.drawCuboid(-1, h - 1, -1, xL + 1, hMax, zL + 1, "stone");
        spec.drawCuboid(0, h + 1, 0, xL, hMax, zL, "air");
        spec.drawCuboid(-1, hMax, -1, xL + 1, hMax, zL, "glowstone");
        spec.drawBlock(p1.iX(), p1.iY(), p1.iZ(), "grass");
        spec.drawBlock(p2.iX(), p2.iY(), p2.iZ(), "glowstone");
        boolean isStep = true;

        BiConsumer<Integer, Integer> drawer;
        if (yOffset > 0) {
            drawer = (x, z) -> {
                for (int i = h; i <= (h + yOffset); i++) {
                    spec.drawBlock(x, i, z, block);
                }
            };
        } else if (yOffset < 0) {
            drawer = (x, z) -> {
                for (int i = h + yOffset; i <= h; i++) {
                    spec.drawBlock(x, i, z, block);
                }
            };
        } else {
            drawer = (x, z) -> spec.drawBlock(x, h + yOffset, z, block);
        }

        for (int z = 0; z < zL - 1; z++) {
            if (isStep) {
                z += randInt(0, stepDistance);
            } else {
                for (int x = 0; x < xL; ) {
                    if (generator.nextBoolean()) {
                        final int xx = randInt(1, 4);
                        for (int i = 0; i < xx; i++) {
                            drawer.accept(x, z);
                            x++;
                        }
                    }
                    x++;
                }
            }
            isStep = !isStep;
        }
        p1.x += 0.5f;
        p2.x += 0.5f;
        p1.y += 2;
        p2.y += 2;
        p1.z += 0.5f;
        p2.z += 0.5f;
        return new ImmutablePair<>(p1, p2);
    }

    public static Pair<IntPoint3D, IntPoint3D> maze(MissionSpec spec, int xL, int zL) {
        final int h = 230;
        final int hMax = 238;
        final IntPoint3D p1 = new IntPoint3D((int) Math.round(xL / 2.0), h, 0);
        final IntPoint3D p2 = new IntPoint3D((int) Math.round(xL / 2.0), h, zL - 1);
        spec.drawCuboid(-1, h - 1, -1, xL, hMax, zL + 1, "stone");
        spec.drawCuboid(0, h + 1, 0, xL - 1, hMax, zL, "air");
        spec.drawCuboid(-1, hMax, -1, xL, hMax, zL + 1, "glowstone");
        MazeGenerator mazeGenerator = new MazeGenerator(zL / 2, xL / 2);
        int x = 0;
        for (boolean[] row : mazeGenerator.convertMaze()) {
            int z = 0;
            for (boolean b : row) {
                spec.drawBlock(x, h + 1, z, b ? "stone" : "air");
                z++;
            }
            x++;
        }
        p1.x += 0.5f;
        p2.x += 0.5f;
        p1.y += 2;
        p2.y += 2;
        p1.z += 0.5f;
        p2.z += 0.5f;
        return new ImmutablePair<>(p1, p2);

    }

    private static int randInt(int min, int max) {
        return generator.nextInt((max - min) + 1) + min;
    }

}
