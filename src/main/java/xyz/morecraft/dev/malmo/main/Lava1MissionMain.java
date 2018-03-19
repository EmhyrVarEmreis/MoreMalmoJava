package xyz.morecraft.dev.malmo.main;

import lombok.extern.slf4j.Slf4j;
import xyz.morecraft.dev.malmo.main.Lava1Mission.Neural;
import xyz.morecraft.dev.malmo.main.Lava1Mission.Recorder;
import xyz.morecraft.dev.malmo.main.Lava1Mission.SimpleNeural;
import xyz.morecraft.dev.malmo.mission.Lava1Mission;
import xyz.morecraft.dev.malmo.proto.MissionRunner;
import xyz.morecraft.dev.malmo.util.Util;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class Lava1MissionMain {

    public static void main(String[] args) throws Exception {
        Util.ensureMalmoXsdPath();
        Util.loadMalmoLib();

        final Map<Integer, MissionRunner<Lava1Mission.Record>> map = new HashMap<>();

        map.put(0, new Recorder());
        map.put(1, new Neural());
        map.put(2, new SimpleNeural());

        new Lava1Mission(args).run(map.get(1));
    }

}
