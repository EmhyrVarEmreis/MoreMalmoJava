package xyz.morecraft.dev.neural;

public abstract class ActivationFunction {

    protected transient double output;

    abstract public double function(double x);

    abstract public double derivative(double x);

}
