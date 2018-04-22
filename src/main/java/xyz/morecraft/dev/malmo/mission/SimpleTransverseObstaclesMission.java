package xyz.morecraft.dev.malmo.mission;

import com.google.gson.reflect.TypeToken;
import com.microsoft.msr.malmo.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import xyz.morecraft.dev.malmo.proto.GoalReachedException;
import xyz.morecraft.dev.malmo.proto.Mission;
import xyz.morecraft.dev.malmo.util.IntPoint3D;
import xyz.morecraft.dev.malmo.util.TerrainGen;
import xyz.morecraft.dev.malmo.util.WorldObservation;
import xyz.morecraft.dev.neural.mlp.neural.InputOutputBundle;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class SimpleTransverseObstaclesMission extends Mission<SimpleTransverseObstaclesMission.Record> {

    public final static String OBSERVE_GRID_0 = "og0";
    public final static String OBSERVE_GRID_1 = "og1";
    public final static String OBSERVE_GRID_2 = "og2";
    public final static String OBSERVE_DISTANCE_1 = "End";
    public final static int OBSERVE_GRID_0_RADIUS = 3;
    public final static int OBSERVE_GRID_1_RADIUS = 7;
    public final static int OBSERVE_GRID_2_WIDTH = 5;
    public final static double tol = 0.25f;

    @Getter
    private Pair<IntPoint3D, IntPoint3D> startingPointWithDestinationPointPair;

    public SimpleTransverseObstaclesMission(String[] argv) {
        super(argv);
    }

    @Override
    protected AgentHost initAgentHost() {
        return new AgentHost();
    }

    @Override
    public IntPoint3D getStartingPoint() {
        return startingPointWithDestinationPointPair.getLeft();
    }

    @Override
    public IntPoint3D getDestinationPoint() {
        return startingPointWithDestinationPointPair.getRight();
    }

    @Override
    protected void isGoalAcquired(AgentHost agentHost, WorldState worldState, WorldObservation worldObservation) throws GoalReachedException {
        if (Objects.nonNull(worldObservation)
                && Objects.nonNull(startingPointWithDestinationPointPair)
                && worldObservation.getPos().withY(0).floor().equals(startingPointWithDestinationPointPair.getRight().withY(0).floor())) {
            throw new GoalReachedException("Destination point reached!");
        }
    }

    @Override
    protected MissionSpec initMissionSpec() {
        MissionSpec missionSpec = new MissionSpec();
        missionSpec.timeLimitInSeconds(600);
        missionSpec.allowAllDiscreteMovementCommands();
        System.out.println();
        StringVector listOfCommandHandlers = missionSpec.getListOfCommandHandlers(0);
        for (int i = 0; i < listOfCommandHandlers.size(); i++) {
            String x = listOfCommandHandlers.get(i);
            System.out.println(x);
            StringVector allowedCommands = missionSpec.getAllowedCommands(0, x);
            for (int l = 0; l < allowedCommands.size(); l++) {
                System.out.println("\t" + allowedCommands.get(l));
            }
        }

        TerrainGen.generator.setSeed(4561); // 777 V2
//        startingPointWithDestinationPointPair = TerrainGen.emptyRoomWithTransverseObstacles(missionSpec, 55, 150, 1, "lava", 0);
        startingPointWithDestinationPointPair = TerrainGen.emptyRoomWithTransverseObstacles(missionSpec, 100, 150, 1, "dirt", 1);
//        startingPointWithDestinationPointPair = TerrainGen.maze(missionSpec, 21, 50);

        final int r0 = Math.floorDiv(OBSERVE_GRID_0_RADIUS, 2);
        final int r1 = Math.floorDiv(OBSERVE_GRID_1_RADIUS, 2);
        missionSpec.observeGrid(-r0, -1, -r0, r0, -1, r0, OBSERVE_GRID_0);
        missionSpec.observeGrid(-r1, -1, -r1, r1, -1, r1, OBSERVE_GRID_1);
        missionSpec.observeGrid(-OBSERVE_GRID_2_WIDTH, -1, 1, OBSERVE_GRID_2_WIDTH, -1, 1, OBSERVE_GRID_2);
        missionSpec.observeDistance(startingPointWithDestinationPointPair.getRight().fX() + 0.5f, startingPointWithDestinationPointPair.getRight().fY() + 1, startingPointWithDestinationPointPair.getRight().fZ() + 0.5f, OBSERVE_DISTANCE_1);
        missionSpec.startAt(startingPointWithDestinationPointPair.getLeft().fX(), startingPointWithDestinationPointPair.getLeft().fY(), startingPointWithDestinationPointPair.getLeft().fZ());
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

        Map<String[][][], Map<Collection<String>, Integer>> map = new TreeMap<>(SimpleTransverseObstaclesMission::compareGrids);

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
        //noinspection ConstantConditions
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

    public static double[] normalizeGrid(String[][][] grid) {
        final double[] t = new double[OBSERVE_GRID_1_RADIUS * OBSERVE_GRID_1_RADIUS];
        int i = 0;
        for (String[] strings : grid[0]) {
            for (String string : strings) {
                t[i++] = string.equalsIgnoreCase("stone") ? 0 : 1;
            }
        }
        return t;
    }

    public static int compareGrids(String[][][] o1, String[][][] o2) {
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
