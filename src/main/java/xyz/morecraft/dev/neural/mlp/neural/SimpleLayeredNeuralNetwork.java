package xyz.morecraft.dev.neural.mlp.neural;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Data;
import xyz.morecraft.dev.neural.ActivationFunction;
import xyz.morecraft.dev.neural.mlp.functions.SoftMax;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@Data
public class SimpleLayeredNeuralNetwork {

    private transient final static Gson gson = new GsonBuilder().create();

    private transient final Function<NeuronLayer, ActivationFunction> activationFunctionSupplier;

    private final List<NeuronLayer> layers;

    private double error;
    private double trainFactor;

    public SimpleLayeredNeuralNetwork() {
        this(SoftMax::new);
    }

    public SimpleLayeredNeuralNetwork(final Function<NeuronLayer, ActivationFunction> activationFunctionSupplier) {
        this.activationFunctionSupplier = activationFunctionSupplier;
        this.layers = new ArrayList<>();
        this.error = Double.MAX_VALUE;
        this.trainFactor = 1.0;
    }

    public void addLayer(int numberOfNeurons, int numberOfInputsPerNeuron) {
        layers.add(new NeuronLayer(numberOfNeurons, numberOfInputsPerNeuron));
    }

    public void rebuild() {
        layers.forEach(NeuronLayer::randomize);
    }

    public void train(double[][] trainingSetInputs, double[][] trainingSetOutputs, int numberOfTrainingIterations) {
        for (int i = 0; i < numberOfTrainingIterations; i++) {
            trainOnce(trainingSetInputs, trainingSetOutputs);
        }
    }

    public void trainOnce(double[][] trainingSetInputs, double[][] trainingSetOutputs) {
        think(trainingSetInputs);

        NeuronLayer prevLayer = null;
        for (int i = layers.size() - 1; i >= 0; i--) {
            final NeuronLayer layer = layers.get(i);
            final NeuronLayer nextLayer = i == 0 ? null : layers.get(i - 1);
            if (Objects.isNull(prevLayer)) {
                layer.setError(NeuralMath.subtract(trainingSetOutputs, layer.getOutput()));
                error = NeuralMath.mean(NeuralMath.abs(layer.getError()));
            } else {
                layer.setError(NeuralMath.dot(prevLayer.getDelta(), NeuralMath.transpose(prevLayer.getSynapticWeights())));
            }
            final ActivationFunction activationFunction = activationFunctionSupplier.apply(layer);
            layer.setDelta(NeuralMath.multiply(layer.getError(), NeuralMath.calc(layer.getOutput(), activationFunction::derivative)));
            if (Objects.isNull(nextLayer)) {
                layer.setAdjustment(NeuralMath.multiply(trainFactor, NeuralMath.dot(NeuralMath.transpose(trainingSetInputs), layer.getDelta())));
            } else {
                layer.setAdjustment(NeuralMath.multiply(trainFactor, NeuralMath.dot(NeuralMath.transpose(nextLayer.getOutput()), layer.getDelta())));
            }
            prevLayer = layer;
        }

        for (NeuronLayer layer : layers) {
            layer.adjust();
        }
    }

    private double[][] thinkLayer(double[][] input, NeuronLayer layer) {
        layer.setInput(input);
        layer.setOutput(NeuralMath.calc(NeuralMath.dot(input, layer.getSynapticWeights()), activationFunctionSupplier.apply(layer)::function));
        return layer.getOutput();
    }

    public double[][] thinkOutput(double[][] trainingSetInputs) {
        final double[][][] think = think(trainingSetInputs);
        return think[think.length - 1];
    }

    public double[] thinkSingleOutput(double[][] trainingSetInputs) {
        return thinkOutput(trainingSetInputs)[0];
    }

    public double[][][] think(double[][] trainingSetInputs) {
        final List<double[][]> outputs = new ArrayList<>();
        outputs.add(thinkLayer(trainingSetInputs, layers.get(0)));
        for (int i = 1; i < layers.size(); i++) {
            outputs.add(thinkLayer(outputs.get(outputs.size() - 1), layers.get(i)));
        }
        return outputs.toArray(new double[outputs.size()][outputs.get(0).length][outputs.get(0)[0].length]);
    }

    public void mutate() {
        layers.forEach(NeuronLayer::mutate);
    }

    public void toFile(String path) throws IOException {
        final FileWriter writer = new FileWriter(path);
        gson.toJson(this, writer);
        writer.flush();
        writer.close();
    }

    public static SimpleLayeredNeuralNetwork fromFile(String path) throws FileNotFoundException {
        return gson.fromJson(new FileReader(path), SimpleLayeredNeuralNetwork.class);
    }
}
