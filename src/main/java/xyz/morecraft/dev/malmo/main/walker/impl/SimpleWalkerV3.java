package xyz.morecraft.dev.malmo.main.walker.impl;

import lombok.extern.slf4j.Slf4j;
import xyz.morecraft.dev.malmo.util.IntPoint3D;

import java.util.Arrays;

@Slf4j
public class SimpleWalkerV3 extends SimpleWalkerV2 {

    private int lastGoDirection;
    private IntPoint3D lastPosition;

    @Override
    protected void calculateGoDirection(final SimpleWalkerV1Data data) {
        super.calculateSimpleGoDirection(data);
        super.adjustDirectionOfTouchedEdges(data);
//        final IntPoint3D roundedCurrentPoint = data.currentPoint.clone().floor();
//        if (Objects.nonNull(lastPosition)) {
//            if (lastPosition.equals(roundedCurrentPoint)) {
//                log.info("samePosition");
//            } else {
//                log.info("newPosition, goDirection={}", data.goDirection);
//                lastPosition = null;
//            }
//            if(lastGoDirection != data.goDirection) {
////                data.goDirection = lastGoDirection;
//                log.info("changing!");
//            }
//        }
//        data.lastPosition = lastPosition;
//        data.lastGoDirection = lastGoDirection;
//        if (Objects.isNull(lastPosition)) {
//            lastPosition = roundedCurrentPoint;
//            lastGoDirection = data.goDirection;
//        }
//        log.info("goDirection={}, lastGoDirection={}, lastPosition={}",
//                data.goDirection,
//                lastGoDirection,
//                lastPosition
//        );
    }

    @Override
    protected void log(SimpleWalkerV1Data data) {
        log.info("angle={}, goalDirection={}, isTouchingEdge={} touchedEdge={}, goDirection={}, originalGoDirection={}, lastGoDirection={}, lastPosition={}, grid={}",
                (int) data.angle,
                data.goalDirection,
                data.isTouchingEdge,
                data.touchedEdge,
                data.goDirection,
                data.originalGoDirection,
                data.lastGoDirection,
                data.lastPosition,
                Arrays.deepToString(data.grid)
        );
    }

}
