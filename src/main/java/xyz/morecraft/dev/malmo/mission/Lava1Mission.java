package xyz.morecraft.dev.malmo.mission;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.microsoft.msr.malmo.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.morecraft.dev.malmo.proto.Mission;
import xyz.morecraft.dev.malmo.util.GlobalKeyListener;
import xyz.morecraft.dev.malmo.util.IntPoint3D;
import xyz.morecraft.dev.malmo.util.TerrainGen;
import xyz.morecraft.dev.malmo.util.TimestampedStringWrapper;
import xyz.morecraft.dev.neural.mlp.neural.InputOutputBundle;
import xyz.morecraft.dev.neural.mlp.neural.SimpleLayeredNeuralNetwork;

import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class Lava1Mission extends Mission<Lava1Mission.Record> {

    private static Logger log = LoggerFactory.getLogger(Lava1Mission.class);

    private final static String OBSERVE_GRID_1 = "og1";
    private final static String OBSERVE_DISTANCE_1 = "End";
    private final static float tol = 0.25f;

    private final GlobalKeyListener globalKeyListener = new GlobalKeyListener();

    private SimpleLayeredNeuralNetwork network;

    public Lava1Mission(String[] argv) {
        super(argv);
    }

    @Override
    protected WorldState step() throws Exception {
        return stepReplay();
//        return stepRecord();
    }

    private WorldState stepRecord() throws Exception {
//            getAgentHost().sendCommand("move 0.5");
        Thread.sleep(333);
        final WorldState worldState = getAgentHost().peekWorldState();
        final TimestampedStringVector observations = worldState.getObservations();
        for (int i = 0; i < observations.size(); i++) {
            final TimestampedString o = observations.get(i);
            final TimestampedStringWrapper ow = new TimestampedStringWrapper(o);
            final float distance = ow.getDistance(OBSERVE_DISTANCE_1);
            final Record record = new Record(globalKeyListener.getKeySet(), ow.getGrid(OBSERVE_GRID_1, 3, 1, 3));
            record(record);
            log.info(
                    "received: keys=[{}], distance={}, grid={}",
                    record.getKeys().stream().collect(Collectors.joining(",")),
                    String.format(Locale.US, "%.03f", distance),
                    record.getGrid()
            );
            if (distance <= tol) {
                log.info("Reached!");
                System.exit(0);
            }
        }
        return worldState;
    }

    private WorldState stepReplay() throws Exception {
        if (Objects.isNull(network)) {
            network = new GsonBuilder().create().fromJson(new FileReader("tmp/0.3406.9.network.json"), SimpleLayeredNeuralNetwork.class);
        }
        final WorldState worldState = getAgentHost().peekWorldState();
        Thread.sleep(50);
        final TimestampedStringVector observations = worldState.getObservations();
        for (int i = 0; i < observations.size(); i++) {
            final TimestampedString o = worldState.getObservations().get(i);
            final TimestampedStringWrapper ow = new TimestampedStringWrapper(o);
            double[] output = network.thinkOutput(new double[][]{normalizeGrid(ow.getGrid(OBSERVE_GRID_1, 3, 1, 3))})[0];

            int max = 0;
            for (int j = 0; j < output.length; j++) {
                if (output[j] > output[max]) {
                    max = j;
                }
            }
            if (max == 0) {
                getAgentHost().sendCommand("move 0.5");
            } else {
                getAgentHost().sendCommand("move 0");
            }
            if (max == 1) {
                getAgentHost().sendCommand("strafe -0.5");
            } else if (max == 2) {
                getAgentHost().sendCommand("strafe 0.5");
            } else {
                getAgentHost().sendCommand("strafe 0");
            }
        }
        return worldState;
    }

    @Override
    protected AgentHost initAgentHost() {
        return new AgentHost();
    }

    @Override
    protected MissionSpec initMissionSpec() {
        MissionSpec missionSpec = new MissionSpec();
        missionSpec.timeLimitInSeconds(25);

        TerrainGen.generator.setSeed(666);
        final Pair<IntPoint3D, IntPoint3D> p = TerrainGen.emptyRoomWithLava(missionSpec, 21, 50, 1);
//        final Pair<IntPoint3D, IntPoint3D> p = TerrainGen.maze(missionSpec, 21, 50);

        missionSpec.observeGrid(-1, -1, -1, 1, -1, 1, OBSERVE_GRID_1);
        missionSpec.observeDistance(p.getRight().x + 0.5f, p.getRight().y + 1, p.getRight().z + 0.5f, OBSERVE_DISTANCE_1);
        missionSpec.startAt(p.getLeft().x, p.getLeft().y, p.getLeft().z);
        missionSpec.setTimeOfDay(12000, false);

        return missionSpec;
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    @Override
    protected MissionRecordSpec initMissionRecordSpec() {
        MissionRecordSpec missionRecordSpec = new MissionRecordSpec("./record/lava1mission.tgz");
//        missionRecordSpec.recordMP4(20, 400000);
//        missionRecordSpec.recordCommands();
//        missionRecordSpec.recordRewards();
//        missionRecordSpec.recordObservations();
        return missionRecordSpec;
    }

    @Override
    protected InputOutputBundle getTrainingSetFromRecord(List<Record> recordList) {
        Set<Record> recordSet = recordList.stream().filter(record -> !record.getKeys().isEmpty()).distinct().collect(Collectors.toSet());

        Map<String[][][], Map<Collection<String>, Integer>> map = new TreeMap<>((o1, o2) -> {
            for (int i = 0; i < o1.length; i++) {
                for (int i1 = 0; i1 < o1[i].length; i1++) {
                    for (int i2 = 0; i2 < o1[i][i1].length; i2++) {
                        if (!o1[i][i1][i2].equalsIgnoreCase(o2[i][i1][i2])) {
                            return 1;
                        }
                    }
                }
            }
            return 0;
        });

        for (Record record : recordSet) {
            final String[][][] grid = record.getGrid();
            final Collection<String> keys = record.getKeys();
            Map<Collection<String>, Integer> m = map.get(grid);
            if (Objects.isNull(m)) {
                m = new TreeMap<>((o1, o2) -> {
                    Set<String> a = new TreeSet<>(String::compareToIgnoreCase);
                    a.addAll(o1);
                    Set<String> b = new TreeSet<>(String::compareToIgnoreCase);
                    b.addAll(o2);
                    return (a.size() == b.size() && a.containsAll(b)) ? 0 : 1;
                });
                map.put(grid, m);
            }
            m.putIfAbsent(keys, 0);
            m.put(keys, m.get(keys) + 1);
        }

        recordSet.clear();
        map.forEach((strings, collectionIntegerMap) -> recordSet.add(new Record(
                collectionIntegerMap.entrySet().stream().max(Comparator.comparing(Map.Entry::getValue)).get().getKey(),
                strings
        )));

        final double[][] input = new double[recordSet.size()][];
        final double[][] output = new double[recordSet.size()][];

        int i = 0;
        for (Record record : recordSet) {
            input[i] = normalizeGrid(record.getGrid());
            output[i] = new double[]{
                    record.getKeys().contains("W") ? 1 : 0,
                    record.getKeys().contains("A") ? 1 : 0,
                    record.getKeys().contains("D") ? 1 : 0
            };
            i++;
        }

        return new InputOutputBundle(
                new String[]{
                        "Grid: 0=All, 1=Lava",
                        "Key list: W A/D"
                },
                input,
                output
        );
    }

    @Override
    protected Type getRecordListTypeToken() {
        return new TypeToken<List<Record>>() {
        }.getType();
    }

    private double[] normalizeGrid(String[][][] grid) {
        final double[] t = new double[9];
        int i = 0;
        for (String[] strings : grid[0]) {
            for (String string : strings) {
                t[i++] = string.equalsIgnoreCase("lava") ? 1 : 0;
            }
        }
        return t;
    }

    @SuppressWarnings("WeakerAccess")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Record {
        private Collection<String> keys;
        private String[][][] grid;
    }

}
