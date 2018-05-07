package xyz.morecraft.dev.malmo.main.walker.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import xyz.morecraft.dev.malmo.alg.EveryDirectionAlgorithm;
import xyz.morecraft.dev.malmo.main.walker.SimpleWalker;
import xyz.morecraft.dev.malmo.proto.MissionWithObserveGrid;
import xyz.morecraft.dev.malmo.util.CardinalDirection;
import xyz.morecraft.dev.malmo.util.IntPoint3D;
import xyz.morecraft.dev.malmo.util.WayUtils;
import xyz.morecraft.dev.malmo.util.WorldObservation;

import java.util.List;

@Slf4j
public class SimpleWalkerB1<T extends MissionWithObserveGrid<?>> extends SimpleWalker<T> {

    public SimpleWalkerB1() {
        super(false);
    }

    @Override
    public int stepInterval() {
        return 250;
    }

    @Override
    protected int getDefaultObserveGridRadius() {
        return 2;
    }

    @Override
    public CardinalDirection calculateNextStep(final WorldObservation worldObservation, MissionWithObserveGrid<?> mission) {
        final String[][][] rawRawGrid = mission.getZeroGrid(worldObservation);
        final String[][] rawGrid = WayUtils.revertGrid(rawRawGrid[0], mission.getDefaultObserveGridWidth());
        final boolean[][] grid = toBooleanGrid(rawGrid, mission.getDefaultObserveGridWidth());

        final IntPoint3D currentPoint = worldObservation.getPos();
        final IntPoint3D destinationPoint = mission.getDestinationPoint();

        gridVisualizer.updateGrid(rawGrid);

        final double angle = WayUtils.getAngle(currentPoint.x, currentPoint.z, destinationPoint.x, destinationPoint.z);

        gridVisualizer.drawAngle(angle);

        final IntPoint3D intersectionPoint = getIntersectionPoint(angle, grid);

        final EveryDirectionAlgorithm everyDirectionAlgorithm = new EveryDirectionAlgorithm(intersectionPoint.iX(), intersectionPoint.iY());
        final Pair<List<IntPoint3D>, List<CardinalDirection>> trace = everyDirectionAlgorithm.calculate(grid);
        final CardinalDirection goDirection = trace.getValue().get(0);

        log.info("goDirection={}, currentPoint={}, destinationPoint={}, angle={}, intersectionPoint={}, trace={}", goDirection, currentPoint, destinationPoint, angle, intersectionPoint, trace.getValue());
//        for (boolean[] booleans : grid) {
//            StringBuilder s = new StringBuilder();
//            for (boolean aBoolean : booleans) {
//                s.append(aBoolean ? 'O' : 'X').append(' ');
//            }
//            log.info(s.toString());
//        }

        gridVisualizer.drawDir(goDirection);

        return goDirection;
    }

    private static final double[][] angles = new double[5][5];

    public static IntPoint3D getIntersectionPoint(double angle, boolean[][] grid) {
        // Very, very dummy method
        int centerX = grid[0].length / 2;
        int centerY = grid.length / 2;
        int x = centerX;
        int y = centerY;
        double localDiff;
        double tmpDiff = Double.MAX_VALUE;
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                if (i == 0 || i == (grid.length - 1) || j == 0 || j == (grid[0].length - 1)) {
                    double newAngle = (WayUtils.getAngle(centerX, centerY, j, i) + 180) % 360;
//                    System.out.println(j + " " + i + " " + String.format("%3.0f", newAngle) + " " + grid[i][j]);
                    angles[i][j] = newAngle;
                    localDiff = Math.abs(newAngle - angle);
                    if (localDiff <= tmpDiff) {
                        tmpDiff = localDiff;
                        y = i;
                        x = j;
                    }
                }
            }
        }
        return new IntPoint3D(x, y, 0);
    }

    public static void main(String[] args) {
        final boolean[][] grid = new boolean[5][5];
//        final double[] angleList = {0, 45, 90, 135, 180, 225, 270, 315};
        final double[] angleList = {117};
        System.out.println();
        for (double angle : angleList) {
            System.out.println(String.format("%3.0f", angle) + "\t" + getIntersectionPoint(angle, grid));
        }
        System.out.println();
        for (double[] aa : angles) {
            for (double a : aa) {
                System.out.print(String.format("%3.0f", a) + " ");
            }
            System.out.println();
        }
    }

}
