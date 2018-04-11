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
        super.calculateGoDirection(data);

        final int originalGoDirection = data.goDirection;
        final int touchedEdge = isTouchingEdges(originalGoDirection, data.currentPoint);
        final boolean isTouchingEdge = touchedEdge != 0;
        if (touchedEdge != -1) {
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
    int isTouchingEdges(final int goDirection, final IntPoint3D currentPoint) {
        if(goDirection == 0 || goDirection == 2) {
            return isTouchingEdgesX(currentPoint);
        } else if(goDirection == 3 || goDirection == 1) {
            return isTouchingEdgesY(currentPoint);
        } else {
            return -1;
        }
    }

    int isTouchingEdgesX(final IntPoint3D currentPoint) {
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

    int isTouchingEdgesY(final IntPoint3D currentPoint) {
        final double yy = Math.abs(currentPoint.y % 1);
        final double yTol = PLAYER_DEPTH / 2 * 1.1;
        if ((1 - yy) <= yTol) {
            return 0;
        } else if (yy <= yTol && yy != 0) {
            return 2;
        } else {
            return -1;
        }
    }

    @Override
    protected void log(SimpleWalkerV1Data data) {
        log.info("angle={}, goalDirection={}, isTouchingEdge={} touchedEdge={}, goDirection={}, originalGoDirection={}, grid={}", (int) data.angle, data.goalDirection, data.isTouchingEdge, data.touchedEdge, data.goDirection, data.originalGoDirection, Arrays.deepToString(data.grid));
    }

}
