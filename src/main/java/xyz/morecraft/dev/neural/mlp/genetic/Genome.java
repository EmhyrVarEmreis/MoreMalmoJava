package xyz.morecraft.dev.neural.mlp.genetic;

import lombok.AllArgsConstructor;
import lombok.Data;
import xyz.morecraft.dev.neural.ActivationFunction;
import xyz.morecraft.dev.neural.mlp.neural.NeuronLayer;

import java.util.function.Function;

@Data
@AllArgsConstructor
public class Genome {

    private int inputCount;
    private int outputCount;
    private int neuronsPerLayer;
    private int layers;
    private Function<NeuronLayer, ActivationFunction> activationFunctionSupplier;

}
