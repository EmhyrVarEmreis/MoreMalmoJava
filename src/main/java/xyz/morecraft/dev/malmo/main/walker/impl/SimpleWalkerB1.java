package xyz.morecraft.dev.malmo.main.walker.impl;

import lombok.extern.slf4j.Slf4j;
import xyz.morecraft.dev.malmo.alg.EveryDirectionAlgorithm;
import xyz.morecraft.dev.malmo.main.walker.SimpleWalker;
import xyz.morecraft.dev.malmo.proto.MissionWithObserveGrid;
import xyz.morecraft.dev.malmo.util.CardinalDirection;
import xyz.morecraft.dev.malmo.util.IntPoint3D;
import xyz.morecraft.dev.malmo.util.WayUtils;
import xyz.morecraft.dev.malmo.util.WorldObservation;

@Slf4j
public class SimpleWalkerB1<T extends MissionWithObserveGrid<?>> extends SimpleWalker<T> {

    @Override
    public int stepInterval() {
        return 20;
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

        final EveryDirectionAlgorithm everyDirectionAlgorithm = new EveryDirectionAlgorithm(0, 0);
        final CardinalDirection goDirection = everyDirectionAlgorithm.calculate(grid).getValue().get(0);

        log.info("goDirection={}", goDirection);

        gridVisualizer.drawDir(goDirection);

        return goDirection;
    }

}
