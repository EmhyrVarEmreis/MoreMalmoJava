package xyz.morecraft.dev.neural;

import xyz.morecraft.dev.neural.mlp.functions.SoftMax;
import xyz.morecraft.dev.neural.mlp.neural.InputOutputBundle;
import xyz.morecraft.dev.neural.mlp.neural.SimpleLayeredNeuralNetwork;

import java.io.IOException;
import java.util.Locale;

public class NeuralMain {

    public static void main(String[] args) throws Exception {
//        SimpleLayeredNeuralNetwork network = new SimpleLayeredNeuralNetwork(SoftMax::new);
//        InputOutputBundle inputOutputBundle = InputOutputBundle.fromFile("record/last.neural.json");
//        network.addLayer(2, inputOutputBundle.getInput()[0].length);
//        network.addLayer(inputOutputBundle.getOutput()[0].length, 2);
//        network.train(inputOutputBundle.getInput(), inputOutputBundle.getOutput(), 1500);
//        System.out.println(network.getError());
//        System.out.println(Arrays.deepToString(network.thinkOutput(new double[][]{inputOutputBundle.getInput()[0]})));
//        network.toFile("record/tmp.json");
//        SimpleLayeredNeuralNetwork network2 = SimpleLayeredNeuralNetwork.fromFile("record/tmp.json");
//        System.out.println(Arrays.deepToString(network2.thinkOutput(new double[][]{inputOutputBundle.getInput()[0]})));

        InputOutputBundle inputOutputBundle = InputOutputBundle.fromFile("record/last.neural.json");
        final int[][] sets = {{9}, {9, 3}, {9, 6}, {9, 9}, {18, 18, 18}};

        for (int[] set : sets) {
            new Thread(() -> {
                try {
                    train(set, inputOutputBundle, 0.05);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    public static void train(int[] layers, InputOutputBundle inputOutputBundle, double tolerance) throws IOException {
        SimpleLayeredNeuralNetwork network = new SimpleLayeredNeuralNetwork(SoftMax::new);
        final StringBuilder suffix = new StringBuilder();
        for (int i = 0; i < layers.length; i++) {
            if (i == 0) {
                network.addLayer(layers[i], inputOutputBundle.getInput()[0].length);
            } else {
                network.addLayer(layers[i], layers[i - 1]);
                suffix.append("-");
            }
            suffix.append(layers[i]);
        }
        network.addLayer(inputOutputBundle.getOutput()[0].length, layers[layers.length - 1]);
        double lastError = Double.MAX_VALUE;
        double lastSavedError = Double.MAX_VALUE;
        int c = 0;
        int sameEpoch = 0;
        do {
            if (sameEpoch >= 100) {
                network.rebuild();
            }
            network.trainOnce(inputOutputBundle.getInput(), inputOutputBundle.getOutput());
            if (network.getError() >= lastError) {
                network.mutate();
                sameEpoch++;
            }
            lastError = network.getError();
            if (c >= 100 && lastSavedError > lastError) {
                network.toFile("tmp/" + String.format(Locale.US, "%.04f", network.getError()) + "." + suffix + ".network.json");
                c = 0;
            }
            c++;
        } while (lastError >= tolerance);
        network.toFile("tmp/" + String.format(Locale.US, "%.04f", network.getError()) + "." + suffix + ".network.json");
        System.out.println("Reached tolerance for: " + suffix);
    }

}
