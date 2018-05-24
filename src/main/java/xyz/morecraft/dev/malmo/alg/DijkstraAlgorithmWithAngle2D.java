package xyz.morecraft.dev.malmo.alg;

import xyz.morecraft.dev.malmo.main.walker.SimpleWalker;
import xyz.morecraft.dev.malmo.util.GridVisualizer;
import xyz.morecraft.dev.malmo.util.IntPoint3D;
import xyz.morecraft.dev.malmo.util.PointIntersection;
import xyz.morecraft.dev.malmo.util.WayUtils;

import java.util.concurrent.TimeUnit;

import static xyz.morecraft.dev.malmo.util.Blocks.BLOCK_STONE;

public class DijkstraAlgorithmWithAngle2D extends DijkstraAlgorithm2D {

    private final double angle;

    public DijkstraAlgorithmWithAngle2D(int goalX, int goalY, double angle) {
        super(goalX, goalY);
        this.angle = angle;
    }

    @Override
    protected double getWeight(int x0, int y0, int x1, int y1) {
//        final double abs = WayUtils.getSymmetricAngle(WayUtils.getCorrectedAngle(x0, y0, x1, y1), angle);
//        if (x0 == 2 && y0 == 2) {
//            weights[x1][y1] = abs;
//        }
        return WayUtils.getSymmetricAngle(WayUtils.getCorrectedAngle(x0, y0, x1, y1), angle);
    }

    private static final double[][] weights = new double[5][5];

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
        final double angle = 22.5;
        System.out.println(((int) angle) + "°");
        gridVisualizer.drawAngle(angle);
        final IntPoint3D intersectionPoint = PointIntersection.getIntersectionPoint(angle, grid);
        System.out.println(intersectionPoint);
        final long startTime = System.nanoTime();
        Algorithm2D dijkstraAlgorithm = new DijkstraAlgorithmWithAngle2D(intersectionPoint.iX(), intersectionPoint.iY(), angle);
        System.out.println(dijkstraAlgorithm.calculate(grid).getValue());
        final long endTime = System.nanoTime();
        System.out.println("time=" + TimeUnit.NANOSECONDS.toMillis(endTime - startTime) + "ms");
        System.out.println();
        for (double[] weight : weights) {
            for (double w : weight) {
                System.out.print(String.format("%4.0f", w));
            }
            System.out.println();
        }
    }

}
