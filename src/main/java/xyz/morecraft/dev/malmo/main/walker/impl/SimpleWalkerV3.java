package xyz.morecraft.dev.malmo.main.walker.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import xyz.morecraft.dev.malmo.util.IntPoint3D;
import xyz.morecraft.dev.malmo.util.WayUtils;

import java.util.NoSuchElementException;
import java.util.Objects;

@Slf4j
public class SimpleWalkerV3 extends SimpleWalkerV2 {

    private int lastGoDirection;
    private boolean changeLastPos;
    private IntPoint3D lastPosition;
    private CustomCircularFifoQueue<IntPoint3D> lastPositionQueue = new CustomCircularFifoQueue<>(2);
    private CustomCircularFifoQueue<Integer> lastGoDirectionQueue = new CustomCircularFifoQueue<>(2);

    @Override
    protected void calculateGoDirection(final SimpleWalkerV1Data data) {
        super.calculateSimpleGoDirection(data);
        super.adjustDirectionOfTouchedEdges(data);
        final IntPoint3D roundedCurrentPoint = data.currentPoint.clone().floor();
        if (!Objects.equals(roundedCurrentPoint, lastPositionQueue.peekLast()) || !Objects.equals(data.goDirection, lastGoDirectionQueue.peekLast())) {
            log.info("Adding {} to {}", roundedCurrentPoint, lastPositionQueue);
            lastPositionQueue.offer(roundedCurrentPoint);
            lastGoDirectionQueue.offer(data.goDirection);
        }
        log.info("Queue: {}", lastPositionQueue);
        if (roundedCurrentPoint.equals(lastPositionQueue.peek()) && Objects.equals(WayUtils.getOppositeSimpleDimension(data.goDirection), lastGoDirectionQueue.peek()) && !Objects.equals(lastGoDirectionQueue.peek(), lastGoDirectionQueue.peekLast())) {
            final int newDirection = WayUtils.getOppositeSimpleDimension(data.goDirection);
            log.warn("Avoiding circular: {}=>{} [{}]", data.goDirection, newDirection, lastGoDirectionQueue);
            data.goDirection = newDirection;
            lastGoDirectionQueue.offer(data.goDirection);
        }
        super.adjustDirectionOfTouchedEdges(data);
    }

    @Override
    protected void log(SimpleWalkerV1Data data) {
//        log.info("angle={}, goalDirection={}, isTouchingEdge={}, touchedEdge={}, goDirection={}, originalGoDirection={}, lastGoDirection={}, lastPosition={}, grid={}",
//                (int) data.angle,
//                data.goalDirection,
//                data.isTouchingEdge,
//                data.touchedEdge,
//                data.goDirection,
//                data.originalGoDirection,
//                data.lastGoDirection,
//                data.lastPosition,
//                Arrays.deepToString(data.grid)
//        );
    }

    private static class CustomCircularFifoQueue<E> extends CircularFifoQueue<E> {
        CustomCircularFifoQueue(int size) {
            super(size);
        }

        E safeGet(int index) {
            try {
                return get(index);
            } catch (NoSuchElementException e) {
                return null;
            }
        }

        E peekLast() {
            return safeGet(size() - 1);
        }
    }

}
