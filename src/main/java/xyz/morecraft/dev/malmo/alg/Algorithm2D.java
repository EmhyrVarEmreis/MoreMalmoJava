package xyz.morecraft.dev.malmo.alg;

import org.apache.commons.lang3.tuple.Pair;
import xyz.morecraft.dev.malmo.util.CardinalDirection;
import xyz.morecraft.dev.malmo.util.IntPoint3D;

import java.util.List;

public interface Algorithm2D {

    Pair<List<IntPoint3D>, List<CardinalDirection>> calculate(final boolean[][] grid);

}
