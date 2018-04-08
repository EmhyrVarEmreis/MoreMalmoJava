package xyz.morecraft.dev.malmo.main.walker.impl;

import lombok.extern.slf4j.Slf4j;
import xyz.morecraft.dev.malmo.main.walker.SimpleWalker;
import xyz.morecraft.dev.malmo.mission.SimpleTransverseObstaclesMission;
import xyz.morecraft.dev.malmo.proto.Mission;
import xyz.morecraft.dev.malmo.util.IntPoint3D;
import xyz.morecraft.dev.malmo.util.WayUtils;
import xyz.morecraft.dev.malmo.util.WorldObservation;

import java.util.Arrays;

@Slf4j
public class SimpleWalkerV1<T extends Mission<?>> extends SimpleWalker<T> {

    @Override
    public int calculateNextStep(final WorldObservation worldObservation, Mission<?> mission) {
        final String[][][] rawRawGrid = worldObservation.getGrid(SimpleTransverseObstaclesMission.OBSERVE_GRID_0, SimpleTransverseObstaclesMission.OBSERVE_GRID_0_RADIUS, 1, SimpleTransverseObstaclesMission.OBSERVE_GRID_0_RADIUS);
        final String[][] rawGrid = WayUtils.revertGrid(rawRawGrid[0], SimpleTransverseObstaclesMission.OBSERVE_GRID_0_RADIUS);

        final IntPoint3D currentPoint = worldObservation.getPos();
        final IntPoint3D destinationPoint = mission.getDestinationPoint();

        return calculateNextStep(rawGrid, currentPoint, destinationPoint);
    }

    /**
     * Directions:
     * <pre>
     *      0
     *  3       1
     *      2
     * </pre>
     *
     * @param rawGrid          Raw 2d grid fetched from WorldObservation.
     * @param currentPoint     Player location.
     * @param destinationPoint Destination location.
     * @return Calculated direction.
     */
    public int calculateNextStep(final String[][] rawGrid, final IntPoint3D currentPoint, final IntPoint3D destinationPoint) {
        final boolean[][] grid = toBooleanGrid(rawGrid, SimpleTransverseObstaclesMission.OBSERVE_GRID_0_RADIUS);

        gridVisualizer.updateGrid(rawGrid);

        final double angle = WayUtils.getAngle(currentPoint.x, currentPoint.z, destinationPoint.x, destinationPoint.z);

        gridVisualizer.drawAngle(destinationPoint.z);

        final int goalDirection = (Math.abs((int) Math.floor(angle / 90.0 + 0.5)) + 3) % 4;
        final int[][] transform = new int[][]{
                {0, 1},
                {1, 2},
                {2, 1},
                {1, 0}
        };

        int goDirection = 0;
        for (int i = 0; i < 4; i++) {
            final int j = (i + goalDirection) % 4;
            final int[] p = transform[j];
            if (grid[p[0]][p[1]]) {
                goDirection = j;
                break;
            }
        }

        gridVisualizer.drawDir(goDirection);

        log.info("angle={}, goalDirection={}, goDirection={}, grid={}", (int) angle, goalDirection, goDirection, Arrays.deepToString(grid));

        return goDirection;
    }

}
