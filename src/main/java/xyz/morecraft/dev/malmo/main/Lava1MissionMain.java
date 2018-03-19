package xyz.morecraft.dev.malmo.main;

import lombok.extern.slf4j.Slf4j;
import xyz.morecraft.dev.malmo.main.Lava1Mission.Neural;
import xyz.morecraft.dev.malmo.main.Lava1Mission.Recorder;
import xyz.morecraft.dev.malmo.main.Lava1Mission.SimpleNeural;
import xyz.morecraft.dev.malmo.main.Lava1Mission.SimpleWalker;
import xyz.morecraft.dev.malmo.mission.Lava1Mission;
import xyz.morecraft.dev.malmo.proto.MissionRunner;
import xyz.morecraft.dev.malmo.util.Util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Slf4j
public class Lava1MissionMain {

    public static void main(String[] args) throws Exception {
        Util.ensureMalmoXsdPath();
        Util.loadMalmoLib();

        final Map<Integer, Supplier<MissionRunner<Lava1Mission>>> map = new HashMap<>();

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
        map.put(3, SimpleWalker::new);

        new Lava1Mission(args).run(map.get(3).get());
    }

}
