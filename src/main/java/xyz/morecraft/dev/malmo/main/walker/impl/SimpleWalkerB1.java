package xyz.morecraft.dev.malmo.main.walker.impl;

import lombok.extern.slf4j.Slf4j;
import xyz.morecraft.dev.malmo.alg.EveryDirectionAlgorithm;
import xyz.morecraft.dev.malmo.main.walker.SimpleWalker;
import xyz.morecraft.dev.malmo.proto.MissionWithObserveGrid;
import xyz.morecraft.dev.malmo.util.CardinalDirection;
import xyz.morecraft.dev.malmo.util.IntPoint3D;
import xyz.morecraft.dev.malmo.util.WayUtils;
import xyz.morecraft.dev.malmo.util.WorldObservation;

@Slf4j
public class SimpleWalkerB1<T extends MissionWithObserveGrid<?>> extends SimpleWalker<T> {

    public SimpleWalkerB1() {
        super(false);
    }

    @Override
    public int stepInterval() {
        return 500;
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
        final CardinalDirection goDirection = everyDirectionAlgorithm.calculate(grid).getValue().get(0);

        log.info("goDirection={}", goDirection);

        gridVisualizer.drawDir(goDirection);

        return goDirection;
    }

    public static IntPoint3D getIntersectionPoint(double angle, boolean[][] grid) {
        // Very, very dummy method
        int centerX = grid[0].length / 2;
        int centerY = grid.length / 2;
        int x = centerX;
        int y = centerY;
        double localDiff;
        double tmpDiff = Double.MAX_VALUE;
        for (int j = 0; j < grid[0].length; j++) {
            for (int i = 0; i < grid.length; i++) {
                if (i == 0 || i == (grid.length - 1) || j == 0 || j == (grid[0].length - 1)) {
                    double newAngle = (WayUtils.getAngle(centerX, centerY, j, i) + 90) % 360;
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
        final boolean[][] grid = {
                {true, true, true},
                {true, true, true},
                {true, true, true}
        };
        final double[] angleList = {0, 45, 90, 135, 180, 225, 270, 315};
        for (double angle : angleList) {
            System.out.println(String.format("%3.0f", angle) + "\t" + getIntersectionPoint(angle, grid));
        }
    }

}
