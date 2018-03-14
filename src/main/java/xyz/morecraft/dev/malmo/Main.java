package xyz.morecraft.dev.malmo;

import xyz.morecraft.dev.malmo.mission.Lava1Mission;
import xyz.morecraft.dev.malmo.util.Util;

public class Main {

    public static void main(String[] args) throws Exception {
        Util.ensureMalmoXsdPath();
        Util.loadMalmoLib();

//        new ExampleMission(args).run();
        new Lava1Mission(args).run();


//        GridVisualizer gridVisualizer = new GridVisualizer();
//        gridVisualizer.setVisible(true);
//        String[][][] strings = {{
//                {
//                        "dirt", "stone", "stone"
//                },
//                {
//                        "stone", "dirt", "stone"
//                },
//                {
//                        "stone", "stone", "dirt"
//                }
//        }};
//        for (int i = 0; i < 10000000; i++) {
//            gridVisualizer.updateGrid(strings);
//            Thread.sleep(1000);
//        }
    }

}
