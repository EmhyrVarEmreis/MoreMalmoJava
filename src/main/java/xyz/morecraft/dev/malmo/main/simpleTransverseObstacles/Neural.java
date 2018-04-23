package xyz.morecraft.dev.malmo.main.simpleTransverseObstacles;

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
import xyz.morecraft.dev.malmo.mission.SimpleTransverseObstaclesMission;
import xyz.morecraft.dev.malmo.proto.MissionRunner;
import xyz.morecraft.dev.malmo.util.GridVisualizer;
import xyz.morecraft.dev.malmo.util.WayUtils;
import xyz.morecraft.dev.malmo.util.WorldObservation;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static xyz.morecraft.dev.malmo.util.Blocks.BLOCK_STONE;

@Slf4j
public class Neural implements MissionRunner<SimpleTransverseObstaclesMission> {

    private final static String multiLayerNetworkPath = "record/multiLayerNetwork.model";
    private GridVisualizer gridVisualizer = new GridVisualizer(true, false);
    private List<Double> lastDistanceQueue = new ArrayList<>(10000);
    private MultiLayerNetwork multiLayerNetwork;

    @Override
    public int stepInterval() {
        return 100;
    }

    @Override
    public void prepare(SimpleTransverseObstaclesMission mission) throws IOException {

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
                                .nIn(mission.getDefaultObserveGridSize())
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

        final SimpleTransverseObstaclesMission.Record[] rawRecords = new GsonBuilder().create().fromJson(new FileReader("record/last.json"), SimpleTransverseObstaclesMission.Record[].class);

        final List<SimpleTransverseObstaclesMission.Record> recordsAll = Arrays.stream(rawRecords)
                .filter(record -> !record.getKeys().isEmpty())
                .filter(record -> record.getKeys().size() == 1)
                .collect(Collectors.toList());

        final Set<SimpleTransverseObstaclesMission.Record> treeSet = new TreeSet<>((o11, o22) -> SimpleTransverseObstaclesMission.compareGrids(o11.getGrid(), o22.getGrid()));
        treeSet.addAll(recordsAll);
        final SimpleTransverseObstaclesMission.Record[] records = treeSet.toArray(new SimpleTransverseObstaclesMission.Record[0]);

        INDArray input = Nd4j.zeros(records.length, mission.getDefaultObserveGridSize());
        INDArray output = Nd4j.zeros(records.length, 3);

        for (int i = 0; i < records.length; i++) {
            final SimpleTransverseObstaclesMission.Record record = records[i];
            int j = 0;
            for (String[][] strings : record.getGrid()) {
                for (String[] string : strings) {
                    for (String s : string) {
                        input.putScalar(new int[]{i, j++}, BLOCK_STONE.equalsIgnoreCase(s) ? 0 : 1);
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
    public WorldState step(AgentHost agentHost, WorldState worldState, WorldObservation worldObservation, SimpleTransverseObstaclesMission mission) {
        if (Objects.isNull(worldObservation)) {
            return worldState;
        }

        final String[][][] rawGrid = mission.getZeroGrid(worldObservation);
        final String[] lineGrid = worldObservation.getGrid(mission.getDefaultObserveGridName(), mission.getDefaultObserveGridSize());
        final double[] grid = SimpleTransverseObstaclesMission.normalizeGrid(rawGrid, mission.getDefaultObserveGridWidth());

        final INDArray input = Nd4j.zeros(mission.getDefaultObserveGridSize());
        for (int j = 0; j < grid.length; j++) {
            input.putScalar(j, grid[j] == 1 ? 1 : 0);
        }

        gridVisualizer.updateGrid(WayUtils.revertGrid(rawGrid[0], mission.getDefaultObserveGridWidth()));

        lastDistanceQueue.add(worldObservation.getDistance(SimpleTransverseObstaclesMission.OBSERVE_DISTANCE_1));

        final float tolerance = 0.25f;
        boolean areTheSame = lastDistanceQueue.size() > 15 && Math.abs(lastDistanceQueue.get(lastDistanceQueue.size() - 1) - lastDistanceQueue.get(lastDistanceQueue.size() - 6)) <= tolerance;
        if (areTheSame) {

            int dir = 0; // -1=A, 1=d
            final int lineGridSplit = lineGrid.length / 2;
            if (!lineGrid[lineGridSplit].equalsIgnoreCase(BLOCK_STONE)) {
                for (int j = 0; j < lineGridSplit; j++) {
                    if (lineGrid[lineGridSplit - j].equalsIgnoreCase(BLOCK_STONE)) {
                        dir = 1;
                        break;
                    } else if (lineGrid[lineGridSplit + j].equalsIgnoreCase(BLOCK_STONE)) {
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