package xyz.morecraft.dev.malmo.main.simpleTransverseObstacles;

import com.google.gson.GsonBuilder;
import com.microsoft.msr.malmo.AgentHost;
import com.microsoft.msr.malmo.WorldState;
import lombok.extern.slf4j.Slf4j;
import xyz.morecraft.dev.malmo.mission.SimpleTransverseObstaclesMission;
import xyz.morecraft.dev.malmo.proto.MissionRunner;
import xyz.morecraft.dev.malmo.util.WorldObservation;
import xyz.morecraft.dev.neural.mlp.neural.SimpleLayeredNeuralNetwork;

import java.io.FileReader;
import java.util.Objects;

@Slf4j
public class SimpleNeural implements MissionRunner<SimpleTransverseObstaclesMission> {

    private SimpleLayeredNeuralNetwork network;

    @Override
    public int stepInterval() {
        return 100;
    }

    @Override
    public void prepare(SimpleTransverseObstaclesMission mission) {
    }

    @Override
    public WorldState step(AgentHost agentHost, WorldState worldState, WorldObservation worldObservation, SimpleTransverseObstaclesMission mission) throws Exception {
        if (Objects.isNull(worldObservation)) {
            return worldState;
        }

        if (Objects.isNull(network)) {
            network = new GsonBuilder().create().fromJson(new FileReader("tmp/0.3406.9.network.json"), SimpleLayeredNeuralNetwork.class);
        }
        Thread.sleep(50);
        double[] output = network.thinkOutput(new double[][]{SimpleTransverseObstaclesMission.normalizeGrid(mission.getZeroGrid(worldObservation), mission.getDefaultObserveGridWidth())})[0];

        int max = 0;
        for (int j = 0; j < output.length; j++) {
            if (output[j] > output[max]) {
                max = j;
            }
        }
        if (max == 0) {
            agentHost.sendCommand("move 0.5");
        } else {
            agentHost.sendCommand("move 0");
        }
        if (max == 1) {
            agentHost.sendCommand("strafe -0.5");
        } else if (max == 2) {
            agentHost.sendCommand("strafe 0.5");
        } else {
            agentHost.sendCommand("strafe 0");
        }

        return worldState;
    }

}