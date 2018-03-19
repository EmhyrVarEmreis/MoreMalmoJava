package xyz.morecraft.dev.malmo.main.Lava1Mission;

import com.microsoft.msr.malmo.AgentHost;
import com.microsoft.msr.malmo.WorldState;
import lombok.extern.slf4j.Slf4j;
import xyz.morecraft.dev.malmo.mission.Lava1Mission;
import xyz.morecraft.dev.malmo.proto.Mission;
import xyz.morecraft.dev.malmo.proto.MissionRunner;
import xyz.morecraft.dev.malmo.util.GlobalKeyListener;
import xyz.morecraft.dev.malmo.util.WorldObservation;

import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class Recorder implements MissionRunner<Lava1Mission.Record> {

    private final GlobalKeyListener globalKeyListener = new GlobalKeyListener();

    @Override
    public int stepInterval() {
        return 500;
    }

    @Override
    public WorldState step(AgentHost agentHost, Mission<Lava1Mission.Record> mission) throws Exception {
        final WorldState worldState = agentHost.peekWorldState();
        final WorldObservation worldObservation = WorldObservation.fromWorldState(worldState);

        if (Objects.isNull(worldObservation)) {
            return worldState;
        }

        final float distance = worldObservation.getDistance(Lava1Mission.OBSERVE_DISTANCE_1);
        final Lava1Mission.Record record = new Lava1Mission.Record(globalKeyListener.getKeySet(), worldObservation.getGrid(Lava1Mission.OBSERVE_GRID_1, Lava1Mission.OBSERVE_GRID_1_RADIUS, 1, Lava1Mission.OBSERVE_GRID_1_RADIUS));
        mission.record(record);
        log.info(
                "received: keys=[{}], distance={}, grid={}",
                record.getKeys().stream().collect(Collectors.joining(",")),
                String.format(Locale.US, "%.03f", distance),
                record.getGrid()
        );
        if (distance <= Lava1Mission.tol) {
            log.info("Reached!");
            System.exit(0);
        }
        return worldState;
    }

}