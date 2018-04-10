package xyz.morecraft.dev.malmo.main.walker;

import lombok.extern.slf4j.Slf4j;
import xyz.morecraft.dev.malmo.main.walker.impl.SimpleWalkerV1;
import xyz.morecraft.dev.malmo.util.IntPoint3D;

import static xyz.morecraft.dev.malmo.util.Blocks.BLOCK_DIRT;
import static xyz.morecraft.dev.malmo.util.Blocks.BLOCK_STONE;

@Slf4j
public class WalkerPlayground {

    public static void main(String[] args) throws InterruptedException {

        SimpleWalkerV1 simpleWalker = new SimpleWalkerV1();

        while (!Thread.currentThread().isInterrupted()) {

            test(simpleWalker);

        }

    }

    private static void test(SimpleWalkerV1 simpleWalker) throws InterruptedException {

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
