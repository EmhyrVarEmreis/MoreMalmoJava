package test;

import xyz.morecraft.dev.malmo.mission.SimpleTransverseObstaclesMission;

import java.util.ArrayList;
import java.util.List;

public class Test {

    public static void main(String[] args) {
        List<SimpleTransverseObstaclesMission.Record> recordList = new ArrayList<>();
        List<String> stringList = new ArrayList<>();
        stringList.add("lava");
        stringList.add("stone");
        List<String[][]> possibilities = new ArrayList<>();
        for (String s : stringList) {
            for (String s1 : stringList) {
                for (String s2 : stringList) {
                    for (String s3 : stringList) {
                        for (String s4 : stringList) {
                            for (String s5 : stringList) {
                                for (String s6 : stringList) {
                                    for (String s7 : stringList) {
                                        for (String s8 : stringList) {
                                            possibilities.add(new String[][]{
                                                    {s, s1, s2},
                                                    {s3, s4, s5},
                                                    {s6, s7, s8}
                                            });
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        for (String[][] possibility : possibilities) {
            recordList.add(new SimpleTransverseObstaclesMission.Record(
                    null,
                    new String[][][]{possibility}
            ));
        }
    }

}
