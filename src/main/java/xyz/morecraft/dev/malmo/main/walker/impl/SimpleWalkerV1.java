package xyz.morecraft.dev.malmo.main.walker.impl;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
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
    public int stepInterval() {
        return 200;
    }

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

        gridVisualizer.drawAngle(angle);

        final int goalDirection = (Math.abs((int) Math.floor(angle / 90.0 + 0.5)) + 3) % 4;
        final int[][] transform = new int[][]{
                {0, 1},
                {1, 2},
                {2, 1},
                {1, 0}
        };

        final SimpleWalkerV1Data data = SimpleWalkerV1Data.builder()
                .currentPoint(currentPoint)
                .angle(angle)
                .goalDirection(goalDirection)
                .grid(grid)
                .transform(transform)
                .build();

        calculateGoDirection(data);

        gridVisualizer.drawDir(data.goDirection);

        log(data);

        return data.goDirection;
    }

    protected void calculateGoDirection(final SimpleWalkerV1Data data) {
        calculateSimpleGoDirection(data);
    }

    protected void calculateSimpleGoDirection(final SimpleWalkerV1Data data) {
        int goDirection = 0;
        for (int i = 0; i < 4; i++) {
            final int j = (i + data.goalDirection) % 4;
            final int[] p = data.transform[j];
            if (data.grid[p[0]][p[1]]) {
                goDirection = j;
                break;
            }
        }
        data.goDirection = goDirection;
    }

    protected void log(final SimpleWalkerV1Data data) {
        log.info("angle={}, goalDirection={}, isTouchingEdge={} touchedEdge={}, goDirection={}, originalGoDirection={}, grid={}", (int) data.angle, data.goalDirection, data.isTouchingEdge, data.touchedEdge, data.goDirection, data.originalGoDirection, Arrays.deepToString(data.grid));
    }

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    protected static class SimpleWalkerV1Data {
        protected IntPoint3D currentPoint;
        protected double angle;
        protected int[][] transform;
        protected boolean[][] grid;
        protected int goDirection;
        protected int goalDirection;
        // V2
        protected int originalGoDirection;
        protected int touchedEdge;
        protected boolean isTouchingEdge;
        // V3
        protected IntPoint3D lastPosition;
        protected int lastGoDirection;
    }

}
