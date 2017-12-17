package xyz.morecraft.dev.neural.mlp.functions;

import xyz.morecraft.dev.neural.ActivationFunction;
import xyz.morecraft.dev.neural.mlp.neural.NeuronLayer;

public class SoftMax extends ActivationFunction {

    private NeuronLayer neuronLayer;

    public SoftMax(final NeuronLayer neuronLayer) {
        this.neuronLayer = neuronLayer;
    }

    @Override
    public double function(double x) {
        double totalLayerInput = 0;
        double max = 0;

        for (final double[] neuron : neuronLayer.getInput()) {
            totalLayerInput += Math.exp(neuron[0] - max);
        }

        output = Math.exp(x - max) / totalLayerInput;
        return output;
    }

    @Override
    public double derivative(double x) {
        return output * (1d - output);
    }

}
