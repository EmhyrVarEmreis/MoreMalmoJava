package xyz.morecraft.dev.malmo.alg;

import org.apache.commons.lang3.tuple.Pair;
import xyz.morecraft.dev.malmo.main.walker.SimpleWalker;
import xyz.morecraft.dev.malmo.main.walker.impl.SimpleWalkerB1;
import xyz.morecraft.dev.malmo.util.CardinalDirection;
import xyz.morecraft.dev.malmo.util.GridVisualizer;
import xyz.morecraft.dev.malmo.util.IntPoint3D;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

import static xyz.morecraft.dev.malmo.util.Blocks.BLOCK_DIRT;
import static xyz.morecraft.dev.malmo.util.Blocks.BLOCK_STONE;
import static xyz.morecraft.dev.malmo.util.WayUtils.CARDINAL_DIRECTION_TRANSLATE_MAP;

public class DijkstraAlgorithm2D implements Algorithm2D {

    private IntPoint3D goal;

    public DijkstraAlgorithm2D(final int goalX, final int goalY) {
        this.goal = new IntPoint3D(goalX, goalY, 0);
    }

    @Override
    public Pair<List<IntPoint3D>, List<CardinalDirection>> calculate(final boolean[][] grid) {
        return calculate(grid, grid[0].length / 2, grid.length / 2, goal.iX(), goal.iY());
    }

    private Pair<List<IntPoint3D>, List<CardinalDirection>> calculate(final boolean[][] grid, final int startX, final int startY, final int goalX, final int goalY) {
        final NavigableSet<Vertex> q = new TreeSet<>();
        // Create vertices
        final Vertex[][] pointGrid = new Vertex[grid.length][grid[0].length];
        final BiFunction<Integer, Integer, Vertex> getVertex = (i, j) -> {
            final Vertex v = pointGrid[i][j];
            if (Objects.isNull(v)) {
                return pointGrid[i][j] = new Vertex(new IntPoint3D(j, i, 0));
            } else {
                return v;
            }
        };
        // Init start & end
        final Vertex start = getVertex.apply(startY, startX);
        final Vertex end = getVertex.apply(goalY, goalX);
        // Init vertices
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                if (!grid[i][j]) {
                    continue;
                }
                final Vertex vertex = getVertex.apply(i, j);
                for (Map.Entry<CardinalDirection, int[]> dirTransform : CARDINAL_DIRECTION_TRANSLATE_MAP.entrySet()) {
                    final int[] transformValue = dirTransform.getValue();
                    final int k = i - transformValue[1];
                    final int l = j - transformValue[0];
                    if (k >= 0 && l >= 0 && k < grid.length && l < grid[i].length && grid[k][l]) {
                        vertex.neighbours.put(getVertex.apply(k, l), dirTransform.getKey());
                    }
                }
            }
        }
        // Real Dijkstra's algorithm
        start.d = 0;
        q.add(start);
        while (!q.isEmpty()) {
            final Vertex u = q.pollFirst();
            if (Objects.isNull(u) || u.compareTo(end) == 0) {
                continue;
            }
            for (Vertex v : u.neighbours.keySet()) {
                if (v.d > (v.d + 1)) {
                    v.d = u.d + 1;
                    v.previous = u;
                    q.add(v);
                }
            }
        }
        // Get Output
        List<IntPoint3D> outPoints = new ArrayList<>();
        List<CardinalDirection> outDirs = new ArrayList<>();
        Vertex v = end;
        do {
            final CardinalDirection dir = v.neighbours.get(v.previous);
            if (Objects.isNull(dir)) {
                continue;
            }
            outPoints.add(v.v);
            outDirs.add(dir);
        } while (Objects.nonNull(v = v.previous));
        Collections.reverse(outPoints);
        Collections.reverse(outDirs);
        return Pair.of(outPoints, outDirs);
    }

    public static void main(String[] args) {
        final GridVisualizer gridVisualizer = new GridVisualizer(true, true);
        final String[][] rawGrid = {
                {BLOCK_STONE, BLOCK_STONE, BLOCK_STONE, BLOCK_STONE, BLOCK_STONE},
                {BLOCK_STONE, BLOCK_DIRT, BLOCK_DIRT, BLOCK_DIRT, BLOCK_DIRT},
                {BLOCK_STONE, BLOCK_DIRT, BLOCK_STONE, BLOCK_STONE, BLOCK_STONE},
                {BLOCK_STONE, BLOCK_DIRT, BLOCK_DIRT, BLOCK_DIRT, BLOCK_STONE},
                {BLOCK_STONE, BLOCK_STONE, BLOCK_STONE, BLOCK_STONE, BLOCK_STONE}
        };
        final boolean[][] grid = SimpleWalker.toBooleanGrid(rawGrid, rawGrid.length);
        gridVisualizer.updateGrid(rawGrid);
        final double angle = 108;
        System.out.println(((int) angle) + "°");
        gridVisualizer.drawAngle(angle);
        final IntPoint3D intersectionPoint = SimpleWalkerB1.getIntersectionPoint(angle, grid);
        System.out.println(intersectionPoint);
        final long startTime = System.nanoTime();
        DijkstraAlgorithm2D dijkstraAlgorithm = new DijkstraAlgorithm2D(intersectionPoint.iX(), intersectionPoint.iY());
        System.out.println(dijkstraAlgorithm.calculate(grid).getValue());
        final long endTime = System.nanoTime();
        System.out.println("time=" + TimeUnit.NANOSECONDS.toMillis(endTime - startTime) + "ms");
    }


    private static class Vertex implements Comparable<Vertex> {
        private final IntPoint3D v;
        private int d = Integer.MAX_VALUE; // MAX_VALUE assumed to be infinity
        private Vertex previous = null;
        private final Map<Vertex, CardinalDirection> neighbours = new HashMap<>();

        private Vertex(IntPoint3D v) {
            this.v = v;
        }

        @Override
        public int hashCode() {
            return v.hashCode();
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public int compareTo(Vertex other) {
            if (d == other.d) {
                return v.compareTo(other.v);
            }
            return Integer.compare(d, other.d);
        }

        @Override
        public String toString() {
            return "Vertex{" +
                    "v=" + v +
                    ", d=" + d +
                    '}';
        }
    }


}
