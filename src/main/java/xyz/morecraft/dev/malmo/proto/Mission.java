package xyz.morecraft.dev.malmo.proto;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.microsoft.msr.malmo.*;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import xyz.morecraft.dev.malmo.util.IntPoint3D;
import xyz.morecraft.dev.malmo.util.WorldObservation;
import xyz.morecraft.dev.neural.mlp.neural.InputOutputBundle;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

public abstract class Mission<Record> {

    private static Logger log = LoggerFactory.getLogger(Mission.class);

    @Getter
    private String[] argv;
    @Getter
    private AgentHost agentHost;
    @Getter
    private MissionSpec missionSpec;
    @Getter
    private MissionRecordSpec missionRecordSpec;
    @Getter
    private List<Record> recordList;

    public Mission(String[] argv) {
        this.argv = argv;
        this.recordList = new LinkedList<>();
    }

    public IntPoint3D getStartingPoint() {
        throw new NotImplementedException();
    }

    public IntPoint3D getDestinationPoint() {
        throw new NotImplementedException();
    }

    protected abstract AgentHost initAgentHost();

    protected abstract MissionSpec initMissionSpec();

    protected abstract MissionRecordSpec initMissionRecordSpec();

    protected abstract void isGoalAcquired(AgentHost agentHost, WorldState worldState, WorldObservation worldObservation) throws GoalReachedException;

    protected InputOutputBundle getTrainingSetFromRecord(List<Record> recordList) {
        throw new UnsupportedOperationException();
    }

    protected Type getRecordListTypeToken() {
        throw new UnsupportedOperationException();
    }

    public void record(Record record) {
        this.recordList.add(record);
    }

    public <T extends Mission<Record>> void run(MissionRunner<T> missionRunner) throws Exception {
        @SuppressWarnings("unchecked") final T thiss = (T) this;
        run((agentHost, worldState, worldObservation) -> {
            Thread.sleep(missionRunner.stepInterval());
            return missionRunner.step(agentHost, worldState, worldObservation, thiss);
        });
    }

//    public <T extends Mission<Record>> void run(UniversalMissionRunner missionRunner) throws Exception {
//        final Mission mission = this;
//        run(agentHost -> {
//            Thread.sleep(missionRunner.stepInterval());
//            return missionRunner.step(agentHost, mission);
//        });
//    }

    private <T extends Mission<Record>> void run(MissionRunnerWrapper missionRunnerWrapper) throws Exception {
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
            worldState = agentHost.peekWorldState();
            for (int i = 0; i < worldState.getErrors().size(); i++) {
                log.error("Error: " + worldState.getErrors().get(i).getText());
            }
        } while (!worldState.getIsMissionRunning());

        boolean isEnd = false;
        do {
            try {
                worldState = agentHost.peekWorldState();
                final WorldObservation worldObservation = WorldObservation.fromWorldState(worldState);
                isGoalAcquired(agentHost, worldState, worldObservation);
                worldState = missionRunnerWrapper.go(agentHost, worldState, worldObservation);
            } catch (GoalReachedException e) {
                log.info("Goal acquired! message=[{}]", e.getMessage());
                isEnd = true;
            }
        } while (worldState.getIsMissionRunning() && !isEnd);

        if (recordList.size() > 0) {
            final String lastJson = "record/last.json";
            final String lastNeural = "record/last.neural.json";

            Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

            try (Writer writer = new FileWriter(lastJson)) {
                gson.toJson(recordList, writer);
            }

            try (Writer writer = new FileWriter(lastNeural)) {
                try (Reader reader = new FileReader(lastJson)) {
                    gson.toJson(getTrainingSetFromRecord(gson.fromJson(reader, getRecordListTypeToken())), writer);
                }
            }
        }

        log.info("Mission has stopped.");
        System.exit(0);
    }

    private static interface MissionRunnerWrapper {
        WorldState go(AgentHost agentHost, WorldState worldState, WorldObservation worldObservation) throws Exception;
    }

}
