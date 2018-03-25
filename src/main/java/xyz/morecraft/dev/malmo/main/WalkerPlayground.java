package xyz.morecraft.dev.malmo.main;

import lombok.extern.slf4j.Slf4j;
import xyz.morecraft.dev.malmo.main.Lava1Mission.SimpleWalker;
import xyz.morecraft.dev.malmo.util.IntPoint3D;

import static xyz.morecraft.dev.malmo.util.BlockNames.BLOCK_DIRT;
import static xyz.morecraft.dev.malmo.util.BlockNames.BLOCK_STONE;

@Slf4j
public class WalkerPlayground {

    public static void main(String[] args) throws InterruptedException {

        SimpleWalker simpleWalker = new SimpleWalker();

        int i = 0;

        while (!Thread.currentThread().isInterrupted()) {

            test(simpleWalker, i++);

        }

    }

    private static void test(SimpleWalker simpleWalker, int xxx) throws InterruptedException {

        final String[][] rawGrid = new String[][]{
                {BLOCK_DIRT, BLOCK_DIRT, BLOCK_STONE, BLOCK_STONE, BLOCK_STONE},
                {BLOCK_DIRT, BLOCK_DIRT, BLOCK_DIRT, BLOCK_DIRT, BLOCK_STONE},
                {BLOCK_DIRT, BLOCK_DIRT, BLOCK_DIRT, BLOCK_DIRT, BLOCK_STONE},
                {BLOCK_DIRT, BLOCK_DIRT, BLOCK_DIRT, BLOCK_DIRT, BLOCK_STONE},
                {BLOCK_DIRT, BLOCK_DIRT, BLOCK_DIRT, BLOCK_STONE, BLOCK_STONE}
        };
        final IntPoint3D currentPoint = new IntPoint3D(0, 0, 0);
        final IntPoint3D destinationPoint = new IntPoint3D(15, 0, 15);

        simpleWalker.calculateNextStep(
                rawGrid, currentPoint, destinationPoint
        );

        Thread.sleep(500);

    }

}
