package xyz.morecraft.dev.malmo.mission.main;

import lombok.extern.slf4j.Slf4j;
import xyz.morecraft.dev.malmo.main.simpleTransverseObstacles.Neural;
import xyz.morecraft.dev.malmo.main.simpleTransverseObstacles.Recorder;
import xyz.morecraft.dev.malmo.main.simpleTransverseObstacles.SimpleNeural;
import xyz.morecraft.dev.malmo.main.walker.impl.SimpleWalkerV1;
import xyz.morecraft.dev.malmo.main.walker.impl.SimpleWalkerV2;
import xyz.morecraft.dev.malmo.main.walker.impl.SimpleWalkerV3;
import xyz.morecraft.dev.malmo.mission.SimpleTransverseObstaclesMission;
import xyz.morecraft.dev.malmo.proto.MissionRunner;
import xyz.morecraft.dev.malmo.util.Util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Slf4j
public class SimpleTransverseObstaclesMissionMain {

    public static void main(String[] args) throws Exception {
        Util.ensureMalmoXsdPath();
        Util.loadMalmoLib();

        final Map<Integer, Supplier<MissionRunner<SimpleTransverseObstaclesMission>>> map = new HashMap<>();

        map.put(0, Recorder::new);
        map.put(1, () -> {
            try {
                return new Neural();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        });
        map.put(2, SimpleNeural::new);
        map.put(3, SimpleWalkerV1::new);
        map.put(4, SimpleWalkerV2::new);
        map.put(5, SimpleWalkerV3::new);

        new SimpleTransverseObstaclesMission(args).run(map.get(5).get());
    }

}
