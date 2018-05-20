package xyz.morecraft.dev.malmo.alg;

import xyz.morecraft.dev.malmo.main.walker.SimpleWalker;
import xyz.morecraft.dev.malmo.util.GridVisualizer;
import xyz.morecraft.dev.malmo.util.IntPoint3D;
import xyz.morecraft.dev.malmo.util.PointIntersection;

import java.util.concurrent.TimeUnit;

import static xyz.morecraft.dev.malmo.util.Blocks.BLOCK_STONE;

public class DijkstraAlgorithmSimple2D extends DijkstraAlgorithm2D {

    public DijkstraAlgorithmSimple2D(int goalX, int goalY) {
        super(goalX, goalY);
    }

    @Override
    protected double getWeight(int x0, int y0, int x1, int y1) {
        return 1.0;
    }

    public static void main(String[] args) {
        final GridVisualizer gridVisualizer = new GridVisualizer(true, true);
        final String[][] rawGrid = {
                {BLOCK_STONE, BLOCK_STONE, BLOCK_STONE, BLOCK_STONE, BLOCK_STONE},
                {BLOCK_STONE, BLOCK_STONE, BLOCK_STONE, BLOCK_STONE, BLOCK_STONE},
                {BLOCK_STONE, BLOCK_STONE, BLOCK_STONE, BLOCK_STONE, BLOCK_STONE},
                {BLOCK_STONE, BLOCK_STONE, BLOCK_STONE, BLOCK_STONE, BLOCK_STONE},
                {BLOCK_STONE, BLOCK_STONE, BLOCK_STONE, BLOCK_STONE, BLOCK_STONE}
        };
        final boolean[][] grid = SimpleWalker.toBooleanGrid(rawGrid, rawGrid.length);
        gridVisualizer.updateGrid(rawGrid);
        final double angle = 359;
        System.out.println(((int) angle) + "°");
        gridVisualizer.drawAngle(angle);
        final IntPoint3D intersectionPoint = PointIntersection.getIntersectionPoint(angle, grid);
        System.out.println(intersectionPoint);
        final long startTime = System.nanoTime();
        Algorithm2D dijkstraAlgorithm = new DijkstraAlgorithmSimple2D(intersectionPoint.iX(), intersectionPoint.iY());
        System.out.println(dijkstraAlgorithm.calculate(grid).getValue());
        final long endTime = System.nanoTime();
        System.out.println("time=" + TimeUnit.NANOSECONDS.toMillis(endTime - startTime) + "ms");
    }

}
