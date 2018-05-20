package xyz.morecraft.dev.malmo.alg;

import org.apache.commons.lang3.tuple.Pair;
import xyz.morecraft.dev.malmo.util.CardinalDirection;
import xyz.morecraft.dev.malmo.util.IntPoint3D;

import java.util.*;
import java.util.function.BiFunction;

import static xyz.morecraft.dev.malmo.util.WayUtils.CARDINAL_DIRECTION_TRANSLATE_MAP;

public abstract class DijkstraAlgorithm2D implements Algorithm2D {

    protected IntPoint3D goal;

    public DijkstraAlgorithm2D(final int goalX, final int goalY) {
        this.goal = new IntPoint3D(goalX, goalY, 0);
    }

    protected abstract double getWeight(int x0, int y0, int x1, int y1);

    @Override
    public Pair<List<IntPoint3D>, List<CardinalDirection>> calculate(final boolean[][] grid) {
        return calculate(grid, grid[0].length / 2, grid.length / 2, goal.iX(), goal.iY());
    }

    private Pair<List<IntPoint3D>, List<CardinalDirection>> calculate(final boolean[][] grid, final int startX, final int startY, final int goalX, final int goalY) {
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
        // Init vertices
        initVertices(grid, getVertex);
        // Init start & end
        final Vertex start = getVertex.apply(startY, startX);
        final Vertex end = getVertex.apply(goalY, goalX);
        // Real Dijkstra's algorithm
        dijkstra(start, end);
        // Get Output
        return getOutput(end);
    }

    private void initVertices(boolean[][] grid, BiFunction<Integer, Integer, Vertex> getVertex) {
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
                        final Vertex vTmp = getVertex.apply(k, l);
                        vertex.neighbours.put(vTmp, new Vertex.VertexNeighbour(vTmp, dirTransform.getKey(), getWeight(j, i, l, k)));
                    }
                }
            }
        }
    }

    private void dijkstra(Vertex start, Vertex end) {
        final NavigableSet<Vertex> q = new TreeSet<>();
        start.distance = 0;
        q.add(start);
        while (!q.isEmpty()) {
            final Vertex u = q.pollFirst();
            if (Objects.isNull(u) || u.compareTo(end) == 0) {
                continue;
            }
            for (Vertex.VertexNeighbour neighbour : u.neighbours.values()) {
                if (neighbour.vertex.distance > (u.distance + neighbour.distance)) {
                    neighbour.vertex.distance = u.distance + neighbour.distance;
                    neighbour.vertex.previous = u;
                    q.add(neighbour.vertex);
                }

            }
        }
    }

    private Pair<List<IntPoint3D>, List<CardinalDirection>> getOutput(Vertex end) {
        List<IntPoint3D> outPoints = new ArrayList<>();
        List<CardinalDirection> outDirs = new ArrayList<>();
        Vertex v = end;
        do {
            final Vertex.VertexNeighbour vertexNeighbour = v.neighbours.get(v.previous);
            if (Objects.isNull(vertexNeighbour)) {
                continue;
            }
            outPoints.add(v.point);
            outDirs.add(vertexNeighbour.direction);
        } while (Objects.nonNull(v = v.previous));
        Collections.reverse(outPoints);
        Collections.reverse(outDirs);
        return Pair.of(outPoints, outDirs);
    }

    private static class Vertex implements Comparable<Vertex> {

        private final IntPoint3D point;
        private final Map<Vertex, VertexNeighbour> neighbours = new HashMap<>();
        private double distance = Integer.MAX_VALUE;
        private Vertex previous = null;

        private Vertex(IntPoint3D point) {
            this.point = point;
        }

        @Override
        public int hashCode() {
            return point.hashCode();
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public int compareTo(Vertex other) {
            if (distance == other.distance) {
                return point.compareTo(other.point);
            }
            return Double.compare(distance, other.distance);
        }

        @Override
        public String toString() {
            return "Vertex{" +
                    "point=" + point +
                    ", distance=" + distance +
                    '}';
        }

        private static class VertexNeighbour {

            private final Vertex vertex;
            private final CardinalDirection direction;
            private final double distance;

            private VertexNeighbour(Vertex vertex, CardinalDirection direction, double distance) {
                this.vertex = vertex;
                this.direction = direction;
                this.distance = distance;
            }

            @Override
            public int hashCode() {
                return vertex.hashCode();
            }

        }

    }

}
