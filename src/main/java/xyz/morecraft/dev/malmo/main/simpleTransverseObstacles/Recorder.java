package xyz.morecraft.dev.malmo.main.simpleTransverseObstacles;

import com.microsoft.msr.malmo.AgentHost;
import com.microsoft.msr.malmo.WorldState;
import lombok.extern.slf4j.Slf4j;
import xyz.morecraft.dev.malmo.mission.SimpleTransverseObstaclesMission;
import xyz.morecraft.dev.malmo.proto.MissionRunner;
import xyz.morecraft.dev.malmo.util.GlobalKeyListener;
import xyz.morecraft.dev.malmo.util.WorldObservation;

import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class Recorder implements MissionRunner<SimpleTransverseObstaclesMission> {

    private final GlobalKeyListener globalKeyListener = new GlobalKeyListener();

    @Override
    public int stepInterval() {
        return 500;
    }

    @Override
    public void prepare(SimpleTransverseObstaclesMission mission) {
    }

    @Override
    public WorldState step(AgentHost agentHost, WorldState worldState, WorldObservation worldObservation, SimpleTransverseObstaclesMission mission) {
        if (Objects.isNull(worldObservation)) {
            return worldState;
        }

        final double distance = worldObservation.getDistance(SimpleTransverseObstaclesMission.OBSERVE_DISTANCE_1);
        final SimpleTransverseObstaclesMission.Record record = new SimpleTransverseObstaclesMission.Record(globalKeyListener.getKeySet(), mission.getZeroGrid(worldObservation));
        mission.record(record);
        log.info(
                "received: keys=[{}], distance={}, grid={}",
                record.getKeys().stream().collect(Collectors.joining(",")),
                String.format(Locale.US, "%.03f", distance),
                record.getGrid()
        );
        if (distance <= SimpleTransverseObstaclesMission.tol) {
            log.info("Reached!");
            System.exit(0);
        }
        return worldState;
    }

}