package xyz.morecraft.dev.malmo.main.walker.impl;

import com.google.common.collect.EvictingQueue;
import lombok.extern.slf4j.Slf4j;
import xyz.morecraft.dev.malmo.util.IntPoint3D;

import java.util.Arrays;

@Slf4j
public class SimpleWalkerV3 extends SimpleWalkerV2 {

    private int lastGoDirection;
    private boolean changeLastPos;
    private EvictingQueue<IntPoint3D> lastPositionQueue = EvictingQueue.create(2);
    private EvictingQueue<Integer> lastGoDirectionQueue = EvictingQueue.create(2);

    @Override
    protected void calculateGoDirection(final SimpleWalkerV1Data data) {
        super.calculateSimpleGoDirection(data);
        final IntPoint3D roundedCurrentPoint = data.currentPoint.clone().floor();
        if (!lastPositionQueue.contains(roundedCurrentPoint)) {
            log.info("Adding {}", roundedCurrentPoint);
            lastPositionQueue.offer(roundedCurrentPoint);
            lastGoDirectionQueue.offer(data.goDirection);
        }
        if (roundedCurrentPoint.equals(lastPositionQueue.peek())) {
//            data.goDirection = WayUtils.getOppositeSimpleDimension(lastGoDirectionQueue.element());
            data.goDirection=lastGoDirectionQueue.element();
            log.info("Avoiding circular => {}", data.goDirection);
        }
//        log.info("{}", lastPositionQueue);
        super.adjustDirectionOfTouchedEdges(data);
    }

    @Override
    protected void log(SimpleWalkerV1Data data) {
        log.info("angle={}, goalDirection={}, isTouchingEdge={}, touchedEdge={}, goDirection={}, originalGoDirection={}, lastGoDirection={}, lastPosition={}, grid={}",
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
