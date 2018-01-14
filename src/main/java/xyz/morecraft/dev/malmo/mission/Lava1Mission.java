package xyz.morecraft.dev.malmo.mission;

import com.microsoft.msr.malmo.*;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.morecraft.dev.malmo.proto.Mission;
import xyz.morecraft.dev.malmo.util.IntPoint3D;
import xyz.morecraft.dev.malmo.util.TerrainGen;
import xyz.morecraft.dev.malmo.util.TimestampedStringWrapper;

import java.util.Locale;

public class Lava1Mission extends Mission {

    private static Logger log = LoggerFactory.getLogger(Lava1Mission.class);

    private final static String OBSERVE_GRID_1 = "og1";
    private final static String OBSERVE_DISTANCE_1 = "End";
    private final static float tol = 0.25f;

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
                log.info(
                        "received: distance={}, grid={}",
                        String.format(Locale.US, "%.03f", distance),
                        ow.getGrid(OBSERVE_GRID_1, 3, 1, 3)
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
        missionSpec.timeLimitInSeconds(1);

        TerrainGen.generator.setSeed(666);
//        final Pair<IntPoint3D, IntPoint3D> p = TerrainGen.emptyRoomWithLava(missionSpec, 21, 50, 1);
        final Pair<IntPoint3D, IntPoint3D> p = TerrainGen.maze(missionSpec, 21, 50);

        missionSpec.observeGrid(-1, -1, -1, 1, -1, 1, OBSERVE_GRID_1);
        missionSpec.observeDistance(p.getRight().x + 0.5f, p.getRight().y + 1, p.getRight().z + 0.5f, OBSERVE_DISTANCE_1);
        missionSpec.startAt(p.getLeft().x, p.getLeft().y, p.getLeft().z);
        missionSpec.setTimeOfDay(12000, false);

        return missionSpec;
    }

    @Override
    protected MissionRecordSpec initMissionRecordSpec() {
        MissionRecordSpec missionRecordSpec = new MissionRecordSpec("./record/lava1mission.tgz");
        missionRecordSpec.recordCommands();
        missionRecordSpec.recordRewards();
        missionRecordSpec.recordObservations();
        return missionRecordSpec;
    }

}
