package xyz.morecraft.dev.malmo.main.walker.impl;

import lombok.extern.slf4j.Slf4j;
import xyz.morecraft.dev.malmo.alg.Algorithm2D;
import xyz.morecraft.dev.malmo.alg.DijkstraAlgorithm2D;
import xyz.morecraft.dev.malmo.util.IntPoint3D;

@Slf4j
public class SimpleWalkerB2 extends SimpleWalkerB1 {

    @Override
    protected Algorithm2D getAlgorithm(IntPoint3D intersectionPoint) {
        return new DijkstraAlgorithm2D(intersectionPoint.iX(), intersectionPoint.iY());
    }

}
