package xyz.morecraft.dev.malmo.main.walker.impl;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import xyz.morecraft.dev.malmo.main.walker.SimpleWalker;
import xyz.morecraft.dev.malmo.proto.MissionWithObserveGrid;
import xyz.morecraft.dev.malmo.util.CardinalDirection;
import xyz.morecraft.dev.malmo.util.IntPoint3D;
import xyz.morecraft.dev.malmo.util.WayUtils;
import xyz.morecraft.dev.malmo.util.WorldObservation;

import java.util.Arrays;

import static xyz.morecraft.dev.malmo.util.CardinalDirection.W;

@Slf4j
public class SimpleWalkerV1<T extends MissionWithObserveGrid<?>> extends SimpleWalker<T> {

    @Override
    public int stepInterval() {
        return 20;
    }

    @Override
    protected int getDefaultObserveGridRadius() {
        return 1;
    }

    @Override
    public CardinalDirection calculateNextStep(final WorldObservation worldObservation, MissionWithObserveGrid<?> mission) {
        final String[][][] rawRawGrid = mission.getZeroGrid(worldObservation);
        final String[][] rawGrid = WayUtils.revertGrid(rawRawGrid[0], mission.getDefaultObserveGridWidth());

        final IntPoint3D currentPoint = worldObservation.getPos();
        final IntPoint3D destinationPoint = mission.getDestinationPoint();

        return calculateNextStep(rawGrid, currentPoint, destinationPoint, mission);
    }

    public CardinalDirection calculateNextStep(final String[][] rawGrid, final IntPoint3D currentPoint, final IntPoint3D destinationPoint, MissionWithObserveGrid<?> mission) {
        final boolean[][] grid = toBooleanGrid(rawGrid, mission.getDefaultObserveGridWidth());

        gridVisualizer.updateGrid(rawGrid);

        final double angle = WayUtils.getAngle(currentPoint.x, currentPoint.z, destinationPoint.x, destinationPoint.z);

        gridVisualizer.drawAngle(angle);

        final CardinalDirection goalDirection = CardinalDirection.values()[(Math.abs((int) Math.floor(angle / 90.0 + 0.5)) + 3) % 4];
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
        CardinalDirection goDirection = W;
        for (int i = 0; i < 2; i++) {
            final int j = (i + data.goalDirection.ordinal()) % 4;
            final int[] p = data.transform[j];
            if (data.grid[p[0]][p[1]]) {
                goDirection = CardinalDirection.values()[j];
                break;
            }
            final int jj = (3 - i + data.goalDirection.ordinal()) % 4;
            final int[] pp = data.transform[jj];
            if (data.grid[pp[0]][pp[1]]) {
                goDirection = CardinalDirection.values()[jj];
                break;
            }
        }
        data.goDirection = goDirection;
    }

    protected void log(final SimpleWalkerV1Data data) {
        log.info("angle={}, goalDirection={}, isTouchingEdge={}, touchedEdge={}, goDirection={}, originalGoDirection={}, grid={}", (int) data.angle, data.goalDirection, data.isTouchingEdge, data.touchedEdge, data.goDirection, data.originalGoDirection, Arrays.deepToString(data.grid));
    }

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    protected static class SimpleWalkerV1Data {
        protected IntPoint3D currentPoint;
        protected double angle;
        protected int[][] transform;
        protected boolean[][] grid;
        protected CardinalDirection goDirection;
        protected CardinalDirection goalDirection;
        // V2
        protected CardinalDirection originalGoDirection;
        protected CardinalDirection touchedEdge;
        protected boolean isTouchingEdge;
    }

}
