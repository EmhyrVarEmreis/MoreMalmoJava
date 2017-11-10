package xyz.morecraft.dev.malmo.mission;

import com.microsoft.msr.malmo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.morecraft.dev.malmo.proto.Mission;
import xyz.morecraft.dev.malmo.util.IntPoint3D;
import xyz.morecraft.dev.malmo.util.TimestampedStringWrapper;

import java.util.Locale;

public class Lava1Mission extends Mission {

    private static Logger log = LoggerFactory.getLogger(Lava1Mission.class);

    private final static String OBSERVE_GRID_1 = "og1";
    private final static String OBSERVE_DISTANCE_1 = "End";
    private final static IntPoint3D p1 = new IntPoint3D(0, 227, 19);
    private final static float tol = 0.25f;

    public Lava1Mission(String[] argv) {
        super(argv);
    }

    @Override
    protected WorldState step() {
        try {
            getAgentHost().sendCommand("move 0.5");
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
        missionSpec.timeLimitInSeconds(5);
        missionSpec.observeGrid(-1, -1, -1, 1, -1, 1, OBSERVE_GRID_1);
        missionSpec.observeDistance(p1.x + 0.5f, p1.y + 1, p1.z + 0.5f, OBSERVE_DISTANCE_1);
        missionSpec.startAt(0.5f, 228, 15.5f);
        missionSpec.setTimeOfDay(12000, false);

        missionSpec.drawCuboid(-50, 227, -50, 50, 237, 50, "air");
        missionSpec.drawCuboid(-5, 227, -2, 5, 227, 20, "stone"); // floor
        missionSpec.drawCuboid(-5, 227, -2, 5, 237, -2, "stone"); // back wall
        missionSpec.drawCuboid(-5, 227, 20, 5, 237, 20, "stone"); // front wall
        missionSpec.drawCuboid(-5, 227, -2, -5, 237, 20, "stone"); // r wall
        missionSpec.drawCuboid(5, 227, -2, 5, 237, 20, "stone"); // l wall
        missionSpec.drawBlock(p1.x, p1.y, p1.z, "grass");

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
