package xyz.morecraft.dev.neural.mlp.neural;

import java.util.function.BiFunction;
import java.util.function.Function;

public class NeuralMath {

    public static double mean(double[][] t) {
        double sum = 0;
        for (double[] d : t) {
            for (double v : d) {
                sum += v;
            }
        }
        return sum / (t.length * t[0].length);
    }

    public static double[][] abs(double[][] t) {
        return calc(t, Math::abs);
    }

    public static double[][] transpose(double[][] m) {
        final double[][] temp = new double[m[0].length][m.length];
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[0].length; j++) {
                temp[j][i] = m[i][j];
            }
        }
        return temp;
    }

    public static double[][] dot(double[][] a, double[][] b) {
        final double[][] result = new double[a.length][b[0].length];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < b[0].length; j++) {
                for (int k = 0; k < a[0].length; k++) {
                    result[i][j] += a[i][k] * b[k][j];
                }
            }
        }
        return result;
    }

    public static double[][] multiply(double[][] a, double[][] b) {
        return dot(a, b);
    }

    public static double[][] multiply(double factor, double[][] a) {
        return calc(a, d -> d * factor);
    }

    public static double[][] subtract(double[][] a, double[][] b) {
        return calc(a, b, (x, y) -> x - y);
    }

    public static double[][] calc(double[][] t, Function<Double, Double> function) {
        final double[][] n = new double[t.length][t[0].length];
        for (int i = 0; i < n.length; i++) {
            for (int j = 0; j < n[0].length; j++) {
                n[i][j] = function.apply(t[i][j]);
            }
        }
        return n;
    }

    public static double[][] calc(double[][] a, double[][] b, BiFunction<Double, Double, Double> function) {
        final double[][] n = new double[a.length][a[0].length];
        for (int i = 0; i < n.length; i++) {
            for (int j = 0; j < n[0].length; j++) {
                n[i][j] = function.apply(a[i][j], b[i][j]);
            }
        }
        return n;
    }

}