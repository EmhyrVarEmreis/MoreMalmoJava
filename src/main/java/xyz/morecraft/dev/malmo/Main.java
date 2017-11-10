package xyz.morecraft.dev.malmo;

import xyz.morecraft.dev.malmo.mission.Lava1Mission;
import xyz.morecraft.dev.malmo.util.Util;

public class Main {

    public static void main(String[] args) {
        Util.ensureMalmoXsdPath();
        Util.loadMalmoLib();

//        new ExampleMission(args).run();
        new Lava1Mission(args).run();
    }

}
