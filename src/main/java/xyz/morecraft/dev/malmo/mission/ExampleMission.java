package xyz.morecraft.dev.malmo.mission;

import com.microsoft.msr.malmo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.morecraft.dev.malmo.proto.Mission;

public class ExampleMission extends Mission {

    private static Logger log = LoggerFactory.getLogger(ExampleMission.class);

    public ExampleMission(String[] argv) {
        super(argv);
    }

    @Override
    protected WorldState step() {
        getAgentHost().sendCommand("move 1");
        getAgentHost().sendCommand("turn " + Math.random());
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            log.error("User interrupted while mission was running.");
            return null;
        }
        final WorldState worldState = getAgentHost().getWorldState();
        log.info(
                "received: video={}, observations={}, rewards={}",
                worldState.getNumberOfVideoFramesSinceLastState(),
                worldState.getNumberOfObservationsSinceLastState(),
                worldState.getNumberOfRewardsSinceLastState()
        );
        for (int i = 0; i < worldState.getRewards().size(); i++) {
            TimestampedReward reward = worldState.getRewards().get(i);
            log.info("Summed reward: " + reward.getValue());
        }
        for (int i = 0; i < worldState.getErrors().size(); i++) {
            TimestampedString error = worldState.getErrors().get(i);
            log.info("Error: " + error.getText());
        }
        return worldState;
    }

    @Override
    protected AgentHost initAgentHost() {
        AgentHost agentHost = new AgentHost();
        try {
            StringVector args = new StringVector();
            args.add("example.JavaExamples_run_mission");
            for (String arg : getArgv()) {
                args.add(arg);
            }
            agentHost.parse(args);
        } catch (Exception e) {
            log.error("ERROR: " + e.getMessage());
            log.error(agentHost.getUsage());
            System.exit(1);
        }
        if (agentHost.receivedArgument("help")) {
            log.error(agentHost.getUsage());
            System.exit(0);
        }
        return agentHost;
    }

    @Override
    protected MissionSpec initMissionSpec() {
        MissionSpec missionSpec = new MissionSpec();
        missionSpec.timeLimitInSeconds(3);
        missionSpec.requestVideo(320, 240);
        missionSpec.rewardForReachingPosition(19.5f, 0.0f, 19.5f, 100.0f, 1.1f);
        return missionSpec;
    }

    @Override
    protected MissionRecordSpec initMissionRecordSpec() {
        MissionRecordSpec missionRecordSpec = new MissionRecordSpec("./saved_data.tgz");
        missionRecordSpec.recordCommands();
        missionRecordSpec.recordMP4(20, 400000);
        missionRecordSpec.recordRewards();
        missionRecordSpec.recordObservations();
        return missionRecordSpec;
    }

}
