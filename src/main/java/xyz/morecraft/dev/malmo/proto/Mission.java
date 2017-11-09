package xyz.morecraft.dev.malmo.proto;

import com.microsoft.msr.malmo.*;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Mission {

    private static Logger log = LoggerFactory.getLogger(Mission.class);

    @Getter
    private String[] argv;
    @Getter
    private AgentHost agentHost;
    @Getter
    private MissionSpec missionSpec;
    @Getter
    private MissionRecordSpec missionRecordSpec;

    public Mission(String[] argv) {
        this.argv = argv;
    }

    protected abstract WorldState step();

    protected abstract AgentHost initAgentHost();

    protected abstract MissionSpec initMissionSpec();

    protected abstract MissionRecordSpec initMissionRecordSpec();

    public void run() {
        log.info("Waiting for the mission to start");

        agentHost = initAgentHost();
        missionSpec = initMissionSpec();
        missionRecordSpec = initMissionRecordSpec();

        try {
            agentHost.startMission(missionSpec, missionRecordSpec);
        } catch (MissionException e) {
            log.error("Error starting mission: " + e.getMessage());
            log.error("Error code: " + e.getMissionErrorCode());
            if (e.getMissionErrorCode() == MissionException.MissionErrorCode.MISSION_INSUFFICIENT_CLIENTS_AVAILABLE) {
                log.error("Is there a Minecraft client running?");
            }
            System.exit(1);
        }

        WorldState worldState;

        do {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                log.error("User interrupted while waiting for mission to start.");
                return;
            }
            worldState = agentHost.getWorldState();
            for (int i = 0; i < worldState.getErrors().size(); i++) {
                log.error("Error: " + worldState.getErrors().get(i).getText());
            }
        } while (!worldState.getIsMissionRunning());

        do {
            worldState = step();
        } while (worldState.getIsMissionRunning());

        log.info("Mission has stopped.");
    }

}
