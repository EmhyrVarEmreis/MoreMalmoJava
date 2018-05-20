package xyz.morecraft.dev.malmo.main.walker.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import xyz.morecraft.dev.malmo.alg.Algorithm2D;
import xyz.morecraft.dev.malmo.alg.EveryDirectionAlgorithm2D;
import xyz.morecraft.dev.malmo.main.walker.SimpleWalker;
import xyz.morecraft.dev.malmo.proto.MissionWithObserveGrid;
import xyz.morecraft.dev.malmo.util.*;

import java.util.List;

@Slf4j
public class SimpleWalkerB1<T extends MissionWithObserveGrid<?>> extends SimpleWalker<T> {

    public SimpleWalkerB1() {
        super(false);
    }

    @Override
    public int stepInterval() {
        return 100;
    }

    @Override
    protected int getDefaultObserveGridRadius() {
        return 2;
    }

    @Override
    public CardinalDirection calculateNextStep(final WorldObservation worldObservation, MissionWithObserveGrid<?> mission) {
        final String[][][] rawRawGrid = mission.getZeroGrid(worldObservation);
        final String[][] rawGrid = WayUtils.revertGrid(rawRawGrid[0], mission.getDefaultObserveGridWidth());
        final boolean[][] grid = toBooleanGrid(rawGrid, mission.getDefaultObserveGridWidth());

        final IntPoint3D currentPoint = worldObservation.getPos();
        final IntPoint3D destinationPoint = mission.getDestinationPoint();

        gridVisualizer.updateGrid(rawGrid);

        final double angle = WayUtils.getAngle(currentPoint.x, currentPoint.z, destinationPoint.x, destinationPoint.z);

        gridVisualizer.drawAngle(angle);

        final IntPoint3D intersectionPoint = PointIntersection.getIntersectionPoint(angle, grid);

        final Algorithm2D algorithm = getAlgorithm(intersectionPoint, angle);
        final Pair<List<IntPoint3D>, List<CardinalDirection>> trace = algorithm.calculate(grid);
        final CardinalDirection goDirection = trace.getValue().get(0);

        log.info("goDirection={}, currentPoint={}, destinationPoint={}, angle={}, intersectionPoint={}, trace={}", goDirection, currentPoint, destinationPoint, angle, intersectionPoint, trace.getValue());
//        for (boolean[] booleans : grid) {
//            StringBuilder s = new StringBuilder();
//            for (boolean aBoolean : booleans) {
//                s.append(aBoolean ? 'O' : 'X').append(' ');
//            }
//            log.info(s.toString());
//        }

        gridVisualizer.drawDir(goDirection);

        return goDirection;
    }

    protected Algorithm2D getAlgorithm(IntPoint3D intersectionPoint, double angle) {
        return new EveryDirectionAlgorithm2D(intersectionPoint.iX(), intersectionPoint.iY());
    }

}
