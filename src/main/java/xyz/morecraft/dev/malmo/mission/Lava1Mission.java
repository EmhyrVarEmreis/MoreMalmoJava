package xyz.morecraft.dev.malmo.mission;

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

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class Lava1Mission extends Mission<Lava1Mission.Record> {

    private static Logger log = LoggerFactory.getLogger(Lava1Mission.class);

    private final static String OBSERVE_GRID_1 = "og1";
    private final static String OBSERVE_DISTANCE_1 = "End";
    private final static float tol = 0.25f;

    private final GlobalKeyListener globalKeyListener = new GlobalKeyListener();

    public Lava1Mission(String[] argv) {
        super(argv);
    }

    @Override
    protected WorldState step() {
        try {
//            getAgentHost().sendCommand("move 0.5");
            Thread.sleep(250);
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected AgentHost initAgentHost() {
        return new AgentHost();
    }

    @Override
    protected MissionSpec initMissionSpec() {
        MissionSpec missionSpec = new MissionSpec();
        missionSpec.timeLimitInSeconds(10);

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
    protected InputOutputBundle getTrainingSetFromJson() {
        Set<Record> recordSet = new HashSet<>(getRecordList());

        final double[][] input = new double[recordSet.size()][];
        final double[][] output = new double[recordSet.size()][];

        int i = 0;
        for (Record record : recordSet) {
            input[i] = new double[9];
            int j = 0;
            for (String[] strings : record.getGrid()[0]) {
                for (String string : strings) {
                    input[i][j++] = string.equalsIgnoreCase("lava") ? 1 : 0;
                }
            }
            output[i] = new double[]{
                    record.getKeys().contains("W") ? 1 : 0,
                    record.getKeys().contains("S") ? 1 : 0,
                    record.getKeys().contains("A") ? 1 : 0,
                    record.getKeys().contains("D") ? 1 : 0
            };
            i++;
        }

        return new InputOutputBundle(
                new String[]{
                        "Grid: 0=All, 1=Lava",
                        "Key list: W S A D"
                },
                input,
                output
        );
    }

    @SuppressWarnings("WeakerAccess")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public class Record {
        private Collection<String> keys;
        private String[][][] grid;
    }

}
