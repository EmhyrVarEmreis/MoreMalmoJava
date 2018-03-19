package xyz.morecraft.dev.malmo.main.Lava1Mission;

import com.google.gson.GsonBuilder;
import com.microsoft.msr.malmo.AgentHost;
import com.microsoft.msr.malmo.WorldState;
import lombok.extern.slf4j.Slf4j;
import xyz.morecraft.dev.malmo.mission.Lava1Mission;
import xyz.morecraft.dev.malmo.proto.MissionRunner;
import xyz.morecraft.dev.malmo.util.WorldObservation;
import xyz.morecraft.dev.neural.mlp.neural.SimpleLayeredNeuralNetwork;

import java.io.FileReader;
import java.util.Objects;

@Slf4j
public class SimpleNeural implements MissionRunner<Lava1Mission> {

    private SimpleLayeredNeuralNetwork network;

    @Override
    public int stepInterval() {
        return 100;
    }

    @Override
    public WorldState step(AgentHost agentHost, Lava1Mission mission) throws Exception {
        final WorldState worldState = agentHost.peekWorldState();
        final WorldObservation worldObservation = WorldObservation.fromWorldState(worldState);

        if (Objects.isNull(worldObservation)) {
            return worldState;
        }

        if (Objects.isNull(network)) {
            network = new GsonBuilder().create().fromJson(new FileReader("tmp/0.3406.9.network.json"), SimpleLayeredNeuralNetwork.class);
        }
        Thread.sleep(50);
        double[] output = network.thinkOutput(new double[][]{Lava1Mission.normalizeGrid(worldObservation.getGrid(Lava1Mission.OBSERVE_GRID_1, Lava1Mission.OBSERVE_GRID_1_RADIUS, 1, Lava1Mission.OBSERVE_GRID_1_RADIUS))})[0];

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