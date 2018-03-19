package xyz.morecraft.dev.malmo.main.Lava1Mission;

import com.google.gson.GsonBuilder;
import com.microsoft.msr.malmo.AgentHost;
import com.microsoft.msr.malmo.WorldState;
import lombok.extern.slf4j.Slf4j;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.distribution.UniformDistribution;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.impl.indexaccum.IAMax;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import xyz.morecraft.dev.malmo.mission.Lava1Mission;
import xyz.morecraft.dev.malmo.proto.Mission;
import xyz.morecraft.dev.malmo.proto.MissionRunner;
import xyz.morecraft.dev.malmo.util.GridVisualizer;
import xyz.morecraft.dev.malmo.util.WorldObservation;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class Neural implements MissionRunner<Lava1Mission.Record> {

    private final static String multiLayerNetworkPath = "record/multiLayerNetwork.model";
    private GridVisualizer gridVisualizer = new GridVisualizer(true);
    private List<Float> lastDistanceQueue = new ArrayList<>(10000);
    private MultiLayerNetwork multiLayerNetwork;

    public Neural() throws IOException {
        int layerNum = 0;
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .iterations(3000)
                .useDropConnect(true)
                .weightInit(WeightInit.XAVIER)
                .activation(Activation.RELU)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .biasInit(1)
                .miniBatch(false)
                .learningRate(0.07)
                .list()
                .layer(layerNum++,
                        new DenseLayer.Builder()
                                .nIn(Lava1Mission.OBSERVE_GRID_1_RADIUS * Lava1Mission.OBSERVE_GRID_1_RADIUS)
                                .nOut(50)
                                .activation(Activation.SOFTMAX)
                                .weightInit(WeightInit.DISTRIBUTION)
                                .dist(new UniformDistribution(0, 1))
                                .build()
                )
                .layer(layerNum++,
                        new DenseLayer.Builder()
                                .nIn(50)
                                .nOut(18)
                                .activation(Activation.SOFTMAX)
                                .weightInit(WeightInit.DISTRIBUTION)
                                .dist(new UniformDistribution(0, 1))
                                .build()
                )
                .layer(layerNum++,
                        new DenseLayer.Builder()
                                .nIn(18)
                                .nOut(9)
                                .activation(Activation.SOFTMAX)
                                .weightInit(WeightInit.DISTRIBUTION)
                                .dist(new UniformDistribution(0, 1))
                                .build()
                )
                .layer(layerNum,
                        new OutputLayer.Builder()
                                .nIn(9)
                                .nOut(3)
                                .activation(Activation.SOFTMAX)
                                .weightInit(WeightInit.DISTRIBUTION)
                                .dist(new UniformDistribution(0, 1))
                                .build()
                )
                .backprop(true)
                .pretrain(false)
                .build();

        multiLayerNetwork = new MultiLayerNetwork(conf);

        final Lava1Mission.Record[] rawRecords = new GsonBuilder().create().fromJson(new FileReader("record/last.json"), Lava1Mission.Record[].class);

        final List<Lava1Mission.Record> recordsAll = Arrays.stream(rawRecords)
                .filter(record -> !record.getKeys().isEmpty())
                .filter(record -> record.getKeys().size() == 1)
                .collect(Collectors.toList());

        final Set<Lava1Mission.Record> treeSet = new TreeSet<>((o11, o22) -> Lava1Mission.compareGrids(o11.getGrid(), o22.getGrid()));
        treeSet.addAll(recordsAll);
        final Lava1Mission.Record[] records = treeSet.toArray(new Lava1Mission.Record[treeSet.size()]);

        INDArray input = Nd4j.zeros(records.length, Lava1Mission.OBSERVE_GRID_1_RADIUS * Lava1Mission.OBSERVE_GRID_1_RADIUS);
        INDArray output = Nd4j.zeros(records.length, 3);

        for (int i = 0; i < records.length; i++) {
            final Lava1Mission.Record record = records[i];
            int j = 0;
            for (String[][] strings : record.getGrid()) {
                for (String[] string : strings) {
                    for (String s : string) {
                        input.putScalar(new int[]{i, j++}, "stone".equalsIgnoreCase(s) ? 0 : 1);
                    }
                }
            }
            Collection<String> keys = record.getKeys();
            output.putScalar(i, 0, keys.contains("W") ? 1 : 0);
            output.putScalar(i, 1, keys.contains("A") ? 1 : 0);
            output.putScalar(i, 2, keys.contains("D") ? 1 : 0);
        }

        DataSet ds = new DataSet(input, output);

        multiLayerNetwork.setListeners(new ScoreIterationListener(100));

        multiLayerNetwork.fit(
                ds
        );

        INDArray result = multiLayerNetwork.output(ds.getFeatureMatrix());
        log.info("{}", result);

        Evaluation eval = new Evaluation(3);
        eval.eval(ds.getLabels(), result);
        log.info("{}", eval.stats());

        ModelSerializer.writeModel(multiLayerNetwork, multiLayerNetworkPath, false);
    }

    @Override
    public int stepInterval() {
        return 100;
    }

    @Override
    public WorldState step(AgentHost agentHost, Mission<Lava1Mission.Record> mission) throws Exception {
        final WorldState worldState = agentHost.peekWorldState();
        final WorldObservation worldObservation = WorldObservation.fromWorldState(worldState);

        if (Objects.isNull(worldObservation)) {
            return worldState;
        }

        final String[][][] rawGrid = worldObservation.getGrid(Lava1Mission.OBSERVE_GRID_1, Lava1Mission.OBSERVE_GRID_1_RADIUS, 1, Lava1Mission.OBSERVE_GRID_1_RADIUS);
        final String[] lineGrid = worldObservation.getGrid(Lava1Mission.OBSERVE_GRID_2, Lava1Mission.OBSERVE_GRID_2_WIDTH * 2 + 1);
        final double[] grid = Lava1Mission.normalizeGrid(rawGrid);

        final INDArray input = Nd4j.zeros(Lava1Mission.OBSERVE_GRID_1_RADIUS * Lava1Mission.OBSERVE_GRID_1_RADIUS);
        for (int j = 0; j < grid.length; j++) {
            input.putScalar(j, grid[j] == 1 ? 1 : 0);
        }

        gridVisualizer.updateGrid(rawGrid);

        lastDistanceQueue.add(worldObservation.getDistance(Lava1Mission.OBSERVE_DISTANCE_1));

        final float tolerance = 0.25f;
        boolean areTheSame = lastDistanceQueue.size() > 15 && Math.abs(lastDistanceQueue.get(lastDistanceQueue.size() - 1) - lastDistanceQueue.get(lastDistanceQueue.size() - 6)) <= tolerance;
        if (areTheSame) {

            int dir = 0; // -1=A, 1=d
            final int lineGridSplit = lineGrid.length / 2;
            if (!lineGrid[lineGridSplit].equalsIgnoreCase("stone")) {
                for (int j = 0; j < lineGridSplit; j++) {
                    if (lineGrid[lineGridSplit - j].equalsIgnoreCase("stone")) {
                        dir = 1;
                        break;
                    } else if (lineGrid[lineGridSplit + j].equalsIgnoreCase("stone")) {
                        dir = -1;
                        break;
                    }
                }
            }

            log.warn(
                    "Retraining; lineGrid={}; dir={}",
                    lineGrid,
                    dir
            );

            multiLayerNetwork.conf().setNumIterations(1);
            if (dir > 0) {
                multiLayerNetwork.fit(input, Nd4j.create(new double[]{0, 0, 1}));
            } else if (dir < 0) {
                multiLayerNetwork.fit(input, Nd4j.create(new double[]{0, 1, 0}));
            } else {
                multiLayerNetwork.fit(input, Nd4j.create(new double[]{1, 0, 0}));
            }
            lastDistanceQueue.clear();
        }

        final INDArray output = multiLayerNetwork.output(input);
        final String[] keys = new String[]{"W", "A", "D"};
        final int max = Nd4j.getExecutioner().execAndReturn(new IAMax(output)).getFinalResult();

        log.info(
                "received: output={}, key={}",
                output,
                keys[max]
        );

        switch (max) {
            case 0:
                agentHost.sendCommand("strafe 0");
                agentHost.sendCommand("move 0.5");
                break;
            case 1:
                agentHost.sendCommand("move 0");
                agentHost.sendCommand("strafe -0.5");
                break;
            case 2:
                agentHost.sendCommand("move 0");
                agentHost.sendCommand("strafe 0.5");
                break;
            default:
                break;
        }

        return worldState;
    }

}