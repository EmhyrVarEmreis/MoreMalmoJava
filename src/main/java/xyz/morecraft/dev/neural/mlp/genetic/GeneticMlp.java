package xyz.morecraft.dev.neural.mlp.genetic;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import xyz.morecraft.dev.neural.ActivationFunction;
import xyz.morecraft.dev.neural.mlp.neural.NeuronLayer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Getter
public class GeneticMlp {

    private final int inputCount;
    private final int outputCount;
    private final int populationMultiplier;
    private final List<Integer> layerCountList;
    private final List<Integer> neuronsPerLayerList;
    private final List<Function<NeuronLayer, ActivationFunction>> activationFunctionSupplierList;

    private final int populationSize;

    private Genome[] genomes;
    private List<NetworkWithGenome> networkWithGenomeList;

    @Builder
    private GeneticMlp(int inputCount, int outputCount, int populationMultiplier, @Singular("layerCount") List<Integer> layerCountList, @Singular("neuronsPerLayer") List<Integer> neuronsPerLayerList, @Singular("activationFunctionSupplier") List<Function<NeuronLayer, ActivationFunction>> activationFunctionSupplierList) {
        this.inputCount = inputCount;
        this.outputCount = outputCount;
        this.layerCountList = layerCountList;
        this.populationMultiplier = populationMultiplier;
        this.neuronsPerLayerList = neuronsPerLayerList;
        this.activationFunctionSupplierList = activationFunctionSupplierList;

        this.generateAllGenomes();
        this.populationSize = genomes.length * populationMultiplier;
        this.generateInitialNetworks();
    }

    private void generateAllGenomes() {
        genomes = new Genome[neuronsPerLayerList.size() * layerCountList.size() * activationFunctionSupplierList.size()];
        int i = 0;
        for (int neuronsPerLayer : neuronsPerLayerList) {
            for (int layerCount : layerCountList) {
                for (Function<NeuronLayer, ActivationFunction> activationFunctionSupplier : activationFunctionSupplierList) {
                    genomes[i++] = new Genome(inputCount, outputCount, neuronsPerLayer, layerCount, activationFunctionSupplier);
                }
            }
        }
    }

    private void generateInitialNetworks() {
        networkWithGenomeList = new ArrayList<>(populationSize);
        for (Genome genome : genomes) {
            for (int i = 0; i < populationMultiplier; i++) {
                networkWithGenomeList.add(NetworkWithGenome.build(genome));
            }
        }
    }

    public void cycle() {

    }

}
