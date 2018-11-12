package xyz.morecraft.dev.malmo.proto;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.microsoft.msr.malmo.*;
import lombok.AllArgsConstructor;
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
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public abstract class Mission<Record> {

    private static Logger log = LoggerFactory.getLogger(Mission.class);

    public static int MAP_GRID_RADIUS = 100;
    public static String MAP_GRID_NAME = "MAP";

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
    @Getter
    private boolean isRunning;

    private long[] times;

    public Mission(String[] argv) {
        this.argv = argv;
        this.recordList = new LinkedList<>();
        this.times = new long[4];
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

    public <T extends Mission<Record>> MissionResult run(MissionRunner<T> missionRunner) throws Exception {
        @SuppressWarnings("unchecked") final T thiss = (T) this;
        missionRunner.prepare(thiss);
        final MissionResult missionResult = run((agentHost, worldState, worldObservation) -> {
            final WorldState tmpState = missionRunner.step(agentHost, worldState, worldObservation, thiss);
            Thread.sleep(missionRunner.stepInterval());
            return tmpState;
        });
        missionRunner.end();
        return missionResult;
    }

//    public <T extends Mission<Record>> void run(UniversalMissionRunner missionRunner) throws Exception {
//        final Mission mission = this;
//        run(agentHost -> {
//            Thread.sleep(missionRunner.stepInterval());
//            return missionRunner.step(agentHost, mission);
//        });
//    }

    private <T extends Mission<Record>> MissionResult run(MissionRunnerWrapper missionRunnerWrapper) throws Exception {
        log.info("Waiting for the mission to start");
        this.isRunning = true;
        times[0] = System.nanoTime();

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
                return null;
            }
            worldState = agentHost.peekWorldState();
            for (int i = 0; i < worldState.getErrors().size(); i++) {
                log.error("Error: " + worldState.getErrors().get(i).getText());
            }
        } while (!worldState.getIsMissionRunning());

        log.info("Mission started!");
        times[1] = System.nanoTime();

        Collection<WorldObservation> worldObservationCollection = new ArrayList<>(10240);
        WorldObservation lastWorldObservation = null;

        boolean isEnd = false;
        do {
            try {
                worldState = agentHost.peekWorldState();
                final WorldObservation worldObservation = WorldObservation.fromWorldState(worldState);
                if (Objects.nonNull(worldObservation)) {
                    if (Objects.isNull(lastWorldObservation) || !worldObservation.getPos().equals(lastWorldObservation.getPos())) {
                        worldObservationCollection.add(worldObservation);
                        lastWorldObservation = worldObservation;
                    }
                }
                isGoalAcquired(agentHost, worldState, worldObservation);
                try {
                    worldState = missionRunnerWrapper.go(agentHost, worldState, worldObservation);
                } catch (NullPointerException ignored) {
                }
            } catch (GoalReachedException e) {
                log.info("Goal acquired! message=[{}]", e.getMessage());
                isEnd = true;
            }
        } while (worldState.getIsMissionRunning() && !isEnd);

        times[2] = System.nanoTime();

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

        times[3] = System.nanoTime();

        this.isRunning = false;
        final WorldState finalWorldState = worldState;
        final MissionResult result = new MissionResult(
                getDTInSeconds(times[0], times[3]),
                getDTInSeconds(times[0], times[1]),
                getDTInSeconds(times[1], times[2]),
                getDTInSeconds(times[2], times[3]),
                IntStream.range(0, (int) worldState.getRewards().size()).mapToDouble(value -> finalWorldState.getRewards().get(value).getValue()).sum(),
                worldObservationCollection
        );
        log.info("Mission has stopped; fullTime={}s, preparingTime={}s, runningTime={}s, finishingTime={}s", formatTime(result.getFullTime()), formatTime(result.getPreparingTime()), formatTime(result.getRunningTime()), formatTime(result.getFinishingTime()));

        return result;
    }

    private static double getDTInSeconds(final long a, final long b) {
        return (b - a) * 1.0 / TimeUnit.SECONDS.toNanos(1) * 1.0;
    }

    private static String formatTime(final double time) {
        return String.format("%.4f", time);
    }

    private interface MissionRunnerWrapper {
        WorldState go(AgentHost agentHost, WorldState worldState, WorldObservation worldObservation) throws Exception;
    }

    @Getter
    @AllArgsConstructor
    public static class MissionResult {
        private double fullTime;
        private double preparingTime;
        private double runningTime;
        private double finishingTime;
        private double reward;
        private Collection<WorldObservation> worldObservationCollection;
    }

}
