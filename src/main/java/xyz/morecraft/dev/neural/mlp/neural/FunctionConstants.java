package xyz.morecraft.dev.neural.mlp.neural;

import java.util.function.Function;

public class FunctionConstants {

    public static Function<Double, Double> SIGMOID = FunctionConstants::sigmoid;

    public static Function<Double, Double> SIGMOID_DERIVATIVE = FunctionConstants::sigmoidDerivative;

    private static double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }

    private static double sigmoidDerivative(double x) {
        return x * (1.0 - x);
    }

}
