package xyz.morecraft.dev.neural.mlp.genetic;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import xyz.morecraft.dev.neural.ActivationFunction;
import xyz.morecraft.dev.neural.mlp.neural.NeuronLayer;
import xyz.morecraft.dev.neural.mlp.neural.SimpleLayeredNeuralNetwork;

import java.util.List;
import java.util.PriorityQueue;
import java.util.function.Function;

@Getter
public class GeneticMlp {

    private final int inputCount;
    private final int outputCount;
    private final int populationMultiplier;
    private final double populationReduceRate;
    private final List<Integer> layerCountList;
    private final List<Integer> neuronsPerLayerList;
    private final List<Function<NeuronLayer, ActivationFunction>> activationFunctionSupplierList;

    private final int populationSize;
    private final int populationRemoveCount;
    private final int populationSurviveCount;
    private Genome[] genomes;

    private PriorityQueue<NetworkWithGenome> networkWithGenomes;

    @Builder
    private GeneticMlp(int inputCount, int outputCount, int populationMultiplier, double populationReduceRate, @Singular("layerCount") List<Integer> layerCountList, @Singular("neuronsPerLayer") List<Integer> neuronsPerLayerList, @Singular("activationFunctionSupplier") List<Function<NeuronLayer, ActivationFunction>> activationFunctionSupplierList) {
        this.inputCount = inputCount;
        this.outputCount = outputCount;
        this.layerCountList = layerCountList;
        this.populationMultiplier = populationMultiplier;
        this.populationReduceRate = populationReduceRate;
        this.neuronsPerLayerList = neuronsPerLayerList;
        this.activationFunctionSupplierList = activationFunctionSupplierList;

        this.generateAllGenomes();
        this.populationSize = genomes.length * populationMultiplier;
        this.populationRemoveCount = (int) Math.round(populationSize * populationReduceRate);
        this.populationSurviveCount = populationSize - populationRemoveCount;
        this.generateInitialNetworks();
    }

    private void generateAllGenomes() {
        genomes = new Genome[neuronsPerLayerList.size() * layerCountList.size() * activationFunctionSupplierList.size()];
        int i = 0;
        for (final int neuronsPerLayer : neuronsPerLayerList) {
            for (final int layerCount : layerCountList) {
                for (Function<NeuronLayer, ActivationFunction> activationFunctionSupplier : activationFunctionSupplierList) {
                    genomes[i++] = new Genome(inputCount, outputCount, neuronsPerLayer, layerCount, activationFunctionSupplier);
                }
            }
        }
    }

    private void generateInitialNetworks() {
        networkWithGenomes = new PriorityQueue<>(populationSize, NetworkWithGenome.COMPARATOR);
        for (final Genome genome : genomes) {
            for (int i = 0; i < populationMultiplier; i++) {
                networkWithGenomes.add(NetworkWithGenome.build(genome));
            }
        }
    }

    public void cycle(Function<SimpleLayeredNeuralNetwork, Double> fitnessFunction) {
        calculateScores(fitnessFunction);
        reducePopulation();
        makeMutations();
        refillPopulation();
    }

    private void calculateScores(Function<SimpleLayeredNeuralNetwork, Double> fitnessFunction) {
        for (final NetworkWithGenome networkWithGenome : networkWithGenomes) {
            networkWithGenome.setScore(
                    fitnessFunction.apply(
                            networkWithGenome.getNeuralNetwork()
                    )
            );
        }
    }

    private void reducePopulation() {
        while (networkWithGenomes.size() > populationSurviveCount) {
            networkWithGenomes.poll();
        }
    }

    private void makeMutations() {
        for (final NetworkWithGenome networkWithGenome : networkWithGenomes) {
            // TODO Make mutations
        }
    }

    private void refillPopulation() {

    }

}
