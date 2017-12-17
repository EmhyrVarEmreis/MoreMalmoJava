package xyz.morecraft.dev.neural.mlp.genetic;

import lombok.AllArgsConstructor;
import lombok.Data;
import xyz.morecraft.dev.neural.mlp.neural.SimpleLayeredNeuralNetwork;

@Data
@AllArgsConstructor
public class NetworkWithGenome {

    private Genome genome;
    private SimpleLayeredNeuralNetwork neuralNetwork;

    public static NetworkWithGenome build(Genome genome) {
        final SimpleLayeredNeuralNetwork neuralNetwork = new SimpleLayeredNeuralNetwork(genome.getActivationFunctionSupplier());
        if (genome.getLayers() == 1) {
            neuralNetwork.addLayer(genome.getOutputCount(), genome.getInputCount());
        } else {
            int newInput;
            int newOutput = genome.getNeuronsPerLayer();
            neuralNetwork.addLayer(newOutput, genome.getInputCount());
            for (int i = 0; i < genome.getLayers() - 2; i++) {
                newInput = genome.getNeuronsPerLayer() + (int) (Math.signum(Math.random() * 2 - 1));
                neuralNetwork.addLayer(newInput, newOutput);
            }
            neuralNetwork.addLayer(genome.getOutputCount(), newOutput);
        }

        return new NetworkWithGenome(genome, null);
    }

}
