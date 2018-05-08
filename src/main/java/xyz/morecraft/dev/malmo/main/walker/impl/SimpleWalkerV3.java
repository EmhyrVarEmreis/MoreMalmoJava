package xyz.morecraft.dev.malmo.main.walker.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.lang3.tuple.MutablePair;
import xyz.morecraft.dev.malmo.util.CardinalDirection;
import xyz.morecraft.dev.malmo.util.IntPoint3D;
import xyz.morecraft.dev.malmo.util.WayUtils;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Objects;

import static xyz.morecraft.dev.malmo.util.CardinalDirection.S;

@Slf4j
public class SimpleWalkerV3 extends SimpleWalkerV2 {

    private CustomCircularFifoQueue<MutablePair<IntPoint3D, CardinalDirection>> lastPositionQueue = new CustomCircularFifoQueue<>(3);

    @Override
    protected void calculateGoDirection(final SimpleWalkerV1Data data) {
        super.calculateSimpleGoDirection(data);
        super.adjustDirectionOfTouchedEdges(data);
        final IntPoint3D roundedCurrentPoint = data.currentPoint.clone().floor();
        final MutablePair<IntPoint3D, CardinalDirection> peek = lastPositionQueue.peek();
        final MutablePair<IntPoint3D, CardinalDirection> peekLast = lastPositionQueue.peekLast();
        final boolean samePosition = Objects.nonNull(peek) && Objects.equals(roundedCurrentPoint, peek.getLeft());
        final boolean samePositionLast = Objects.nonNull(peekLast) && Objects.equals(roundedCurrentPoint, peekLast.getLeft());
        if (samePosition) {
            final int[] p = data.transform[0];
            final boolean canGoSouth = data.grid[p[0]][p[1]];
            final CardinalDirection newDir = canGoSouth ? S : WayUtils.getOppositeSimpleDimension(data.goDirection);
            final int[] pp = data.transform[newDir.ordinal()];
            final boolean canGo = data.grid[pp[0]][pp[1]];
            if (canGo) {
                log.info("Adjusting goDirection from {} to {} (canGoSouth={})", data.goDirection, newDir, canGoSouth);
                data.goDirection = newDir;
            }
        }
        if (Objects.isNull(peekLast) || !samePositionLast) {
            log.info("Adding <{};{}> to {}", roundedCurrentPoint, data.goDirection, lastPositionQueue);
            lastPositionQueue.offer(MutablePair.of(roundedCurrentPoint, data.goDirection));
        }
        if (samePositionLast && peekLast.getRight() != data.goDirection) {
            log.info("Altering current with new direction {}", roundedCurrentPoint, data.goDirection);
            peekLast.setRight(data.goDirection);
        }
        super.adjustDirectionOfTouchedEdges(data);
    }

    @Override
    protected void log(SimpleWalkerV1Data data) {
        log.info("angle={}, goalDirection={}, isTouchingEdge={}, touchedEdge={}, goDirection={}, originalGoDirection={}, grid={}",
                (int) data.angle,
                data.goalDirection,
                data.isTouchingEdge,
                data.touchedEdge,
                data.goDirection,
                data.originalGoDirection,
                Arrays.deepToString(data.grid)
        );
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
