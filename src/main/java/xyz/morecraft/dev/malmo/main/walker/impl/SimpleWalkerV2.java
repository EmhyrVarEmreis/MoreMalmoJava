package xyz.morecraft.dev.malmo.main.walker.impl;

import lombok.extern.slf4j.Slf4j;
import xyz.morecraft.dev.malmo.util.IntPoint3D;
import xyz.morecraft.dev.malmo.util.WayUtils;

import java.util.Arrays;

import static xyz.morecraft.dev.malmo.util.Constants.PLAYER_DEPTH;
import static xyz.morecraft.dev.malmo.util.Constants.PLAYER_WIDTH;

@Slf4j
public class SimpleWalkerV2 extends SimpleWalkerV1 {

    @Override
    protected void calculateGoDirection(final SimpleWalkerV1Data data) {
        super.calculateGoDirection(data);
        adjustDirectionOfTouchedEdges(data);
    }

    protected void adjustDirectionOfTouchedEdges(final SimpleWalkerV1Data data) {
        final int originalGoDirection = data.goDirection;
        final int touchedEdge = isTouchingEdges(originalGoDirection, data.currentPoint, data.transform, data.grid);
        final boolean isTouchingEdge = touchedEdge != -1;
        if (isTouchingEdge) {
            data.goDirection = WayUtils.getOppositeSimpleDimension(touchedEdge);
        }
        data.originalGoDirection = originalGoDirection;
        data.isTouchingEdge = isTouchingEdge;
        data.touchedEdge = touchedEdge;
    }

    /**
     * Checks if player in given location is touching edges
     *
     * @param goDirection  Direction of movement
     * @param currentPoint Current location
     * @return direction of touched edge or -1 if no edge is touched
     */
    private int isTouchingEdges(final int goDirection, final IntPoint3D currentPoint, final int[][] transform, final boolean[][] grid) {
        if ((goDirection == 0 || goDirection == 2) && canTouchEdge(goDirection, transform, grid)) {
            return isTouchingEdgesX(currentPoint);
        } else if ((goDirection == 3 || goDirection == 1) && canTouchEdge(goDirection, transform, grid)) {
            return isTouchingEdgesZ(currentPoint);
        } else {
            return -1;
        }
    }

    private boolean canTouchEdge(final int goDirection, final int[][] transform, final boolean[][] grid) {
        final int[] p1 = Arrays.copyOf(transform[(goDirection + 1) % 4], 2);
        final int[] p2 = Arrays.copyOf(transform[(goDirection + 3) % 4], 2);
        final int index = goDirection % 2;
        final int adjustment = (goDirection == 1 || goDirection == 2) ? 1 : -1;
        p1[index] = p1[index] + adjustment;
        p2[index] = p2[index] + adjustment;
        return !grid[p1[0]][p1[1]] || !grid[p2[0]][p2[1]];
    }

    private int isTouchingEdgesX(final IntPoint3D currentPoint) {
        final double xx = Math.abs(currentPoint.x % 1);
        final double xTol = PLAYER_WIDTH / 2 * 1.1;
        if ((1 - xx) <= xTol) {
            return 3;
        } else if (xx <= xTol && xx != 0) {
            return 1;
        } else {
            return -1;
        }
    }

    private int isTouchingEdgesZ(final IntPoint3D currentPoint) {
        final double zz = Math.abs(currentPoint.z % 1);
        final double zTol = PLAYER_DEPTH / 2 * 1.1;
        if ((1 - zz) <= zTol) {
            return 0;
        } else if (zz <= zTol && zz != 0) {
            return 2;
        } else {
            return -1;
        }
    }

    @Override
    protected void log(SimpleWalkerV1Data data) {
        log.info("angle={}, goalDirection={}, isTouchingEdge={}, touchedEdge={}, goDirection={}, originalGoDirection={}, grid={}", (int) data.angle, data.goalDirection, data.isTouchingEdge, data.touchedEdge, data.goDirection, data.originalGoDirection, Arrays.deepToString(data.grid));
    }

}
