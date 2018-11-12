package xyz.morecraft.dev.malmo.mission;

import com.google.gson.reflect.TypeToken;
import com.microsoft.msr.malmo.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import xyz.morecraft.dev.malmo.proto.GoalReachedException;
import xyz.morecraft.dev.malmo.proto.MissionWithObserveGrid;
import xyz.morecraft.dev.malmo.util.IntPoint3D;
import xyz.morecraft.dev.malmo.util.TerrainGen;
import xyz.morecraft.dev.malmo.util.WorldObservation;
import xyz.morecraft.dev.neural.mlp.neural.InputOutputBundle;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class SimpleTransverseObstaclesMission extends MissionWithObserveGrid<SimpleTransverseObstaclesMission.Record> {

    public final static String OBSERVE_DISTANCE_1 = "End";
    public final static double tol = 0.25f;

    private static AgentHost AGENT_HOST;

    private TerrainGen.Result terrainGenResult;

    public SimpleTransverseObstaclesMission(String[] argv, int defaultObserveGridRadius) {
        super(argv, defaultObserveGridRadius);
    }

    @Override
    protected synchronized AgentHost initAgentHost() {
        if (Objects.isNull(AGENT_HOST)) {
            AGENT_HOST = new AgentHost();
        }
        return AGENT_HOST;
    }

    @Override
    public IntPoint3D getStartingPoint() {
        return terrainGenResult.getStartingPoint();
    }

    @Override
    public IntPoint3D getDestinationPoint() {
        return terrainGenResult.getDestinationPoint();
    }

    @Override
    protected void isGoalAcquired(AgentHost agentHost, WorldState worldState, WorldObservation worldObservation) throws GoalReachedException {
    }

    @Override
    protected MissionSpec initMissionSpec() {
        MissionSpec missionSpec = new MissionSpec();
        missionSpec.timeLimitInSeconds(20);
        missionSpec.allowAllDiscreteMovementCommands();

        log.info("Allowed commands");
        StringVector listOfCommandHandlers = missionSpec.getListOfCommandHandlers(0);
        for (int i = 0; i < listOfCommandHandlers.size(); i++) {
            String x = listOfCommandHandlers.get(i);
            log.info("{} --->", x);
            StringVector allowedCommands = missionSpec.getAllowedCommands(0, x);
            for (int l = 0; l < allowedCommands.size(); l++) {
                log.info(allowedCommands.get(l));
            }
            log.info("{} <--- ", x);
        }

//        missionSpec.forceWorldReset();
        TerrainGen.generator.setSeed(4561); // 777 V2
//        startingPointWithDestinationPointPair = TerrainGen.emptyRoomWithTransverseObstacles(missionSpec, 55, 150, 1, "lava", 0);
        terrainGenResult = TerrainGen.emptyRoomWithTransverseObstacles(missionSpec, 20, 20, 1, "dirt", 1);
//        startingPointWithDestinationPointPair = TerrainGen.maze(missionSpec, 21, 50);

        missionSpec.rewardForReachingPosition(
                terrainGenResult.getDestinationPoint().x.floatValue(),
                terrainGenResult.getDestinationPoint().y.floatValue(),
                terrainGenResult.getDestinationPoint().z.floatValue(),
                100,
                (float) 0.49
        );
        missionSpec.endAt(
                terrainGenResult.getDestinationPoint().x.floatValue(),
                terrainGenResult.getDestinationPoint().y.floatValue(),
                terrainGenResult.getDestinationPoint().z.floatValue(),
                (float) 0.49
        );

        missionSpec.observeGrid(-getDefaultObserveGridRadius(), -1, -getDefaultObserveGridRadius(), getDefaultObserveGridRadius(), -1, getDefaultObserveGridRadius(), getDefaultObserveGridName());
        missionSpec.observeGrid(-MAP_GRID_RADIUS, -1, -MAP_GRID_RADIUS, MAP_GRID_RADIUS, -1, MAP_GRID_RADIUS, MAP_GRID_NAME);
        missionSpec.observeDistance(terrainGenResult.getDestinationPoint().fX() + 0.5f, terrainGenResult.getDestinationPoint().fY() + 1, terrainGenResult.getDestinationPoint().fZ() + 0.5f, OBSERVE_DISTANCE_1);
        missionSpec.startAt(terrainGenResult.getStartingPoint().fX(), terrainGenResult.getStartingPoint().fY(), terrainGenResult.getStartingPoint().fZ());
        missionSpec.setTimeOfDay(12000, false);

        return missionSpec;
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    @Override
    protected MissionRecordSpec initMissionRecordSpec() {
        MissionRecordSpec missionRecordSpec = new MissionRecordSpec("./record/lava1mission.tgz");
        return missionRecordSpec;
    }

    @Override
    protected InputOutputBundle getTrainingSetFromRecord(List<Record> recordList) {
        Set<Record> recordSet = recordList.stream().filter(record -> !record.getKeys().isEmpty()).collect(Collectors.toSet());

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
        //noinspection OptionalGetWithoutIsPresent
        map.forEach((strings, collectionIntegerMap) -> recordSet.add(new Record(
                collectionIntegerMap.entrySet().stream().max(Comparator.comparing(Map.Entry::getValue)).get().getKey(),
                strings
        )));

        final double[][] input = new double[recordSet.size()][];
        final double[][] output = new double[recordSet.size()][];

        int i = 0;
        for (Record record : recordSet) {
            input[i] = normalizeGrid(record.getGrid(), getDefaultObserveGridWidth());
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

    public static double[] normalizeGrid(String[][][] grid, final int width) {
        final double[] t = new double[width * width];
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
