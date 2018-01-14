package test;

import xyz.morecraft.dev.neural.mlp.functions.Sigmoid;
import xyz.morecraft.dev.neural.mlp.functions.SoftMax;
import xyz.morecraft.dev.neural.mlp.genetic.GeneticMlp;
import xyz.morecraft.dev.neural.mlp.genetic.NetworkWithGenome;
import xyz.morecraft.dev.neural.mlp.neural.NeuralMath;

import java.util.PriorityQueue;

public class Playground1 {

    public static void main(String[] args) {

        // XOR GATE
        final double[][] trainingSetInputs = {
                {0, 0, 1},
                {0, 1, 1},
                {1, 0, 1},
                {0, 1, 0},
                {1, 0, 0},
                {1, 1, 1},
                {0, 0, 0},
                {1, 1, 0}
        };
        final double[][] trainingSetOutputs = {
                {0, 1, 1, 1, 1, 0, 0, 1}
        };

        final GeneticMlp geneticMlp = GeneticMlp.builder()
                .inputCount(3)
                .outputCount(1)
                .layerCount(1)
                .layerCount(2)
                .layerCount(3)
                .layerCount(4)
                .neuronsPerLayer(2)
                .neuronsPerLayer(3)
                .neuronsPerLayer(4)
                .neuronsPerLayer(5)
                .populationMultiplier(3)
                .populationReduceRate(0.2)
                .activationFunctionSupplier(nL -> new Sigmoid())
                .activationFunctionSupplier(SoftMax::new)
                .build();

        geneticMlp.cycle(
                network -> {
                    return NeuralMath.mean(
                            NeuralMath.abs(
                                    NeuralMath.subtract(
                                            trainingSetOutputs[0],
                                            NeuralMath.transpose(network.thinkOutput(trainingSetInputs))[0]
                                    )
                            )
                    );
                }
        );

        final PriorityQueue<NetworkWithGenome> networkWithGenomes = new PriorityQueue<>(NetworkWithGenome.COMPARATOR);
        networkWithGenomes.addAll(geneticMlp.getNetworkWithGenomes());

        System.out.println(networkWithGenomes.poll().getScore());

//        while (!networkWithGenomes.isEmpty()) {
//            System.out.println(networkWithGenomes.poll().getScore());
//        }

//        System.out.println(Arrays.deepToString(NeuralMath.dot(
//                new double[][]{{0, 1, 1, 1}},
//                NeuralMath.transpose(new double[][]{{0, 1, 1, 1}})
//        )));
//
//        System.out.println(NeuralMath.mean(
//                NeuralMath.abs(new double[][]{{0, -1, 1, -1}})
//        ));
//
//        System.out.println(FunctionConstants.SIGMOID.apply(5.0));
//        System.out.println(FunctionConstants.SIGMOID_DERIVATIVE.apply(5.0));
//
//
//        SimpleLayeredNeuralNetwork neuralNetwork = new SimpleLayeredNeuralNetwork(
//                SoftMax::new
//        );
//        neuralNetwork.setTrainFactor(1);
//        neuralNetwork.addLayer(1, 3);
//
//        neuralNetwork.getLayers().get(0).setSynapticWeights(
//                new double[][]{{0.5}, {0.5}, {0.5}}
//        );
//
//        neuralNetwork.train(trainingSetInputs, NeuralMath.transpose(trainingSetOutputs), 1000);
//
//        System.out.println();
//
////        System.out.println(neuralNetwork.getError());
//        System.out.println(
//                neuralNetwork.thinkOutput(
//                        new double[][]{{1, 1, 0}}
//                )[0]
//        );
//
//        System.out.println();
//
//        for (NeuronLayer neuronLayer : neuralNetwork.getLayers()) {
//            System.out.println(Arrays.deepToString(neuronLayer.getSynapticWeights()));
//        }

    }

}
