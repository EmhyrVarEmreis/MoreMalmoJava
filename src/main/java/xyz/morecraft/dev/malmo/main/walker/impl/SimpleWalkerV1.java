package xyz.morecraft.dev.malmo.main.walker.impl;

import lombok.extern.slf4j.Slf4j;
import xyz.morecraft.dev.malmo.main.walker.SimpleWalker;
import xyz.morecraft.dev.malmo.mission.SimpleTransverseObstaclesMission;
import xyz.morecraft.dev.malmo.proto.Mission;
import xyz.morecraft.dev.malmo.util.IntPoint3D;
import xyz.morecraft.dev.malmo.util.WayUtils;
import xyz.morecraft.dev.malmo.util.WorldObservation;

import java.util.Arrays;

import static xyz.morecraft.dev.malmo.util.Constants.PLAYER_WIDTH;

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

        int goDirection = 0;
        for (int i = 0; i < 4; i++) {
            final int j = (i + goalDirection) % 4;
            final int[] p = transform[j];
            if (grid[p[0]][p[1]]) {
                goDirection = j;
                break;
            }
        }

        final int originalGoDirection = goDirection;
        final int touchedEdge = isTouchingEdges(currentPoint);
        final boolean isTouchingEdge = touchedEdge != 0;
        if (goDirection == 0) {
            if (touchedEdge != 0) {
                goDirection = (touchedEdge == -1) ? 1 : 3;
            }
        }

        gridVisualizer.drawDir(goDirection);

        log.info("angle={}, goalDirection={}, isTouchingEdge={} touchedEdge={}, goDirection={}, originalGoDirection={}, grid={}", (int) angle, goalDirection, isTouchingEdge, touchedEdge, goDirection, originalGoDirection, Arrays.deepToString(grid));

        return goDirection;
    }

    /**
     * Checks if player in given location is touching edges
     *
     * @param currentPoint Current location
     * @return -1 => touching left<br/>0 => no touching<br/>1 => touching right
     */
    private int isTouchingEdges(final IntPoint3D currentPoint) {
        final double xx = Math.abs(currentPoint.x % 1);
        final double tol = PLAYER_WIDTH / 2 * 1.1;
        if ((1 - xx) <= tol) {
            return -1;
        } else if (xx <= tol) {
            return 1;
        } else {
            return 0;
        }
    }

}
