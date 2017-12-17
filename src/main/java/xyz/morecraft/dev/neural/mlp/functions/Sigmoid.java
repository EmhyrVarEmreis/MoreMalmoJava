package xyz.morecraft.dev.neural.mlp.functions;

import xyz.morecraft.dev.neural.ActivationFunction;

public class Sigmoid extends ActivationFunction {

    @Override
    public double function(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }

    @Override
    public double derivative(double x) {
        return x * (1.0 - x);
    }

}
