package xyz.morecraft.dev.malmo;

import xyz.morecraft.dev.malmo.mission.ExampleMission;
import xyz.morecraft.dev.malmo.util.Util;

public class Main {

    public static void main(String[] args) {
        Util.ensureMalmoXsdPath();
        Util.loadMalmoLib();

        new ExampleMission(args).run();
    }

}
