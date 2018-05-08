package xyz.morecraft.dev.malmo.main.walker.impl;

import lombok.extern.slf4j.Slf4j;
import xyz.morecraft.dev.malmo.util.CardinalDirection;
import xyz.morecraft.dev.malmo.util.IntPoint3D;
import xyz.morecraft.dev.malmo.util.WayUtils;

import java.util.Arrays;
import java.util.Objects;

import static xyz.morecraft.dev.malmo.util.CardinalDirection.*;
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
        final CardinalDirection originalGoDirection = data.goDirection;
        final CardinalDirection touchedEdge = isTouchingEdges(originalGoDirection, data.currentPoint, data.transform, data.grid);
        final boolean isTouchingEdge = Objects.nonNull(touchedEdge);
        if (isTouchingEdge) {
            data.goDirection = WayUtils.getOppositeSimpleDimension(touchedEdge);
        }
        data.originalGoDirection = originalGoDirection;
        data.isTouchingEdge = isTouchingEdge;
        data.touchedEdge = touchedEdge;
    }

    private CardinalDirection isTouchingEdges(final CardinalDirection goDirection, final IntPoint3D currentPoint, final int[][] transform, final boolean[][] grid) {
        if ((goDirection == S || goDirection == N) && canTouchEdge(goDirection, transform, grid)) {
            return isTouchingEdgesX(currentPoint);
        } else if ((goDirection == W || goDirection == E) && canTouchEdge(goDirection, transform, grid)) {
            return isTouchingEdgesZ(currentPoint);
        } else {
            return null;
        }
    }

    private boolean canTouchEdge(final CardinalDirection goDirection, final int[][] transform, final boolean[][] grid) {
        final int goDirOrdinal = goDirection.ordinal();
        final int[] p1 = Arrays.copyOf(transform[(goDirOrdinal + 1) % 4], 2);
        final int[] p2 = Arrays.copyOf(transform[(goDirOrdinal + 3) % 4], 2);
        final int index = goDirOrdinal % 2;
        final int adjustment = (goDirection == W || goDirection == N) ? 1 : -1;
        p1[index] = p1[index] + adjustment;
        p2[index] = p2[index] + adjustment;
        return !grid[p1[0]][p1[1]] || !grid[p2[0]][p2[1]];
    }

    private CardinalDirection isTouchingEdgesX(final IntPoint3D currentPoint) {
        final double xx = Math.abs(currentPoint.x % 1);
        final double xTol = PLAYER_WIDTH / 2;
        return checkIfTouchingAndGetDir(xx, xTol, E, W);
    }

    private CardinalDirection isTouchingEdgesZ(final IntPoint3D currentPoint) {
        final double zz = Math.abs(currentPoint.z % 1);
        final double zTol = PLAYER_DEPTH / 2;
        return checkIfTouchingAndGetDir(zz, zTol, S, N);
    }

    private CardinalDirection checkIfTouchingAndGetDir(double value, double tolerance, CardinalDirection w, CardinalDirection e) {
        if ((1 - value) < tolerance) {
            return w;
        } else if (value <= tolerance && value != 0) {
            return e;
        } else {
            return null;
        }
    }

    @Override
    protected void log(SimpleWalkerV1Data data) {
        log.info("angle={}, goalDirection={}, isTouchingEdge={}, touchedEdge={}, goDirection={}, originalGoDirection={}, grid={}", (int) data.angle, data.goalDirection, data.isTouchingEdge, data.touchedEdge, data.goDirection, data.originalGoDirection, Arrays.deepToString(data.grid));
    }

}
