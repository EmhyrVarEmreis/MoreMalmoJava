package xyz.morecraft.dev.malmo.alg;

import org.apache.commons.lang3.tuple.Pair;
import xyz.morecraft.dev.malmo.main.walker.SimpleWalker;
import xyz.morecraft.dev.malmo.main.walker.impl.SimpleWalkerB1;
import xyz.morecraft.dev.malmo.util.CardinalDirection;
import xyz.morecraft.dev.malmo.util.GridVisualizer;
import xyz.morecraft.dev.malmo.util.IntPoint3D;
import xyz.morecraft.dev.malmo.util.WayUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static xyz.morecraft.dev.malmo.util.Blocks.BLOCK_DIRT;
import static xyz.morecraft.dev.malmo.util.Blocks.BLOCK_STONE;
import static xyz.morecraft.dev.malmo.util.CardinalDirection.*;

public class EveryDirectionAlgorithm {

    private final static Map<CardinalDirection, int[]> someAlgorithmDirTransform;
    private final TreeSet<Pair<List<IntPoint3D>, List<CardinalDirection>>> traces;
    private int minLength;
    private IntPoint3D goal;

    public EveryDirectionAlgorithm(final int goalX, final int goalY) {
        traces = new TreeSet<>(Comparator.comparingDouble(this::calculateWeight));
        goal = new IntPoint3D(goalX, goalY, 0);
        minLength = 0;
    }

    private double calculateWeight(Pair<List<IntPoint3D>, List<CardinalDirection>> trace) {
        final List<IntPoint3D> key = trace.getKey();
        double weight = key.size();
        if (!key.isEmpty()) {
            final IntPoint3D end = key.get(key.size() - 1);
            weight += Math.pow(Math.hypot(end.x - goal.x, end.y - goal.y), 4) * 1.01;
            final List<CardinalDirection> value = trace.getValue();
            if (value.get(value.size() - 1) == NONE) {
                weight = Double.MAX_VALUE;
            }
        }
        return weight;
    }

    public Pair<List<IntPoint3D>, List<CardinalDirection>> calculate(final boolean[][] grid) {
        final Pair<List<IntPoint3D>, List<CardinalDirection>> trace = Pair.of(new ArrayList<>(), new ArrayList<>());
        final int x = grid[0].length / 2;
        final int y = grid.length / 2;
        trace.getKey().add(new IntPoint3D(x, y, 0));
        trace.getValue().add(NONE);
        calculate(grid, x, y, trace);
        if (traces.isEmpty()) {
            return null;
        } else {
            final Pair<List<IntPoint3D>, List<CardinalDirection>> firstTrace = traces.first();
            if (firstTrace.getKey().size() > 1) {
                firstTrace.getKey().remove(0);
                firstTrace.getValue().remove(0);
            }
            return firstTrace;
        }
    }

    private void calculate(final boolean[][] grid, final int x, final int y, final Pair<List<IntPoint3D>, List<CardinalDirection>> trace) {
        for (Map.Entry<CardinalDirection, int[]> dirTransform : someAlgorithmDirTransform.entrySet()) {
            final CardinalDirection dir = dirTransform.getKey();
            final int[] transform = dirTransform.getValue();
            final int newX = x + transform[0];
            final int newY = y + transform[1];
            final IntPoint3D newPoint = new IntPoint3D(newX, newY, 0);
            if (x == goal.x && y == goal.y) {
                traces.add(trace);
                minLength = trace.getKey().size();
            } else if ((minLength == 0 || trace.getKey().size() < minLength)
                    && newX >= 0 && newX < grid[0].length && newY >= 0 && newY < grid.length && grid[newY][newX]
                    && (trace.getValue().isEmpty() || (!trace.getValue().get(trace.getValue().size() - 1).equals(WayUtils.getOppositeSimpleDimension(dir)) && !trace.getKey().contains(newPoint)))) {
                final Pair<List<IntPoint3D>, List<CardinalDirection>> newTrace = Pair.of(new ArrayList<>(trace.getKey()), new ArrayList<>(trace.getValue()));
                newTrace.getKey().add(newPoint);
                newTrace.getValue().add(dir);
                calculate(grid, newX, newY, newTrace);
            } else {
                traces.add(trace);
            }
        }
    }

    static {
        someAlgorithmDirTransform = new LinkedHashMap<>();
        someAlgorithmDirTransform.put(S, new int[]{0, -1});
        someAlgorithmDirTransform.put(W, new int[]{1, 0});
        someAlgorithmDirTransform.put(N, new int[]{0, 1});
        someAlgorithmDirTransform.put(E, new int[]{-1, 0});
    }

    public static void main(String[] args) {
        final GridVisualizer gridVisualizer = new GridVisualizer(true, true);
        final String[][] rawGrid = {
                {BLOCK_STONE, BLOCK_STONE, BLOCK_STONE, BLOCK_STONE, BLOCK_STONE},
                {BLOCK_STONE, BLOCK_STONE, BLOCK_STONE, BLOCK_DIRT, BLOCK_DIRT},
                {BLOCK_STONE, BLOCK_STONE, BLOCK_STONE, BLOCK_STONE, BLOCK_STONE},
                {BLOCK_DIRT, BLOCK_DIRT, BLOCK_DIRT, BLOCK_DIRT, BLOCK_DIRT},
                {BLOCK_STONE, BLOCK_STONE, BLOCK_STONE, BLOCK_STONE, BLOCK_STONE}
        };
        final boolean[][] grid = SimpleWalker.toBooleanGrid(rawGrid, rawGrid.length);
        gridVisualizer.updateGrid(rawGrid);
//        final double angle = WayUtils.getCorrectedAngle(rawGrid.length / 2, rawGrid.length / 2, rawGrid.length / 2, rawGrid.length - 1);
        final double angle = 108;
        System.out.println(((int) angle) + "°");
        gridVisualizer.drawAngle(angle);
        final IntPoint3D intersectionPoint = SimpleWalkerB1.getIntersectionPoint(angle, grid);
//        final IntPoint3D intersectionPoint = new IntPoint3D(1, 0, 0);
        System.out.println(intersectionPoint);
        final long startTime = System.nanoTime();
        final EveryDirectionAlgorithm everyDirectionAlgorithm = new EveryDirectionAlgorithm(intersectionPoint.iX(), intersectionPoint.iY());
        final Pair<List<IntPoint3D>, List<CardinalDirection>> cardinalDirections = everyDirectionAlgorithm.calculate(grid);
        final long endTime = System.nanoTime();
        System.out.println(cardinalDirections.getValue());
        System.out.println("time=" + TimeUnit.NANOSECONDS.toMillis(endTime - startTime) + "ms");
    }

}
