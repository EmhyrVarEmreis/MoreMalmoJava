package xyz.morecraft.dev.malmo.mission;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.microsoft.msr.malmo.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;
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
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.morecraft.dev.malmo.proto.Mission;
import xyz.morecraft.dev.malmo.util.GlobalKeyListener;
import xyz.morecraft.dev.malmo.util.IntPoint3D;
import xyz.morecraft.dev.malmo.util.TerrainGen;
import xyz.morecraft.dev.malmo.util.TimestampedStringWrapper;
import xyz.morecraft.dev.neural.mlp.neural.InputOutputBundle;
import xyz.morecraft.dev.neural.mlp.neural.SimpleLayeredNeuralNetwork;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class Lava1Mission extends Mission<Lava1Mission.Record> {

    private static Logger log = LoggerFactory.getLogger(Lava1Mission.class);

    private final static String OBSERVE_GRID_1 = "og1";
    private final static String OBSERVE_DISTANCE_1 = "End";
    private final static int OBSERVE_GRID_1_RADIUS = 5;
    private final static float tol = 0.25f;

    private final GlobalKeyListener globalKeyListener = new GlobalKeyListener();

    private SimpleLayeredNeuralNetwork network;

    private final static String multiLayerNetworkPath = "record/multiLayerNetwork.model";
    private MultiLayerNetwork multiLayerNetwork;

    private static boolean isRecord = false;
    private static boolean isDL4J = true;

    public Lava1Mission(String[] argv) throws IOException {
        super(argv);
        if (!isRecord) {
            if (isDL4J) {
                initDL4J();
            }
        }
    }

    @Override
    protected WorldState step() throws Exception {
        return isRecord ? stepRecord() : (isDL4J ? stepReplayDL4J() : stepReplay());
    }

    private WorldState stepRecord() throws Exception {
//            getAgentHost().sendCommand("move 0.5");
        Thread.sleep(500);
        final WorldState worldState = getAgentHost().peekWorldState();
        final TimestampedStringVector observations = worldState.getObservations();
        for (int i = 0; i < observations.size(); i++) {
            final TimestampedString o = observations.get(i);
            final TimestampedStringWrapper ow = new TimestampedStringWrapper(o);
            final float distance = ow.getDistance(OBSERVE_DISTANCE_1);
            final Record record = new Record(globalKeyListener.getKeySet(), ow.getGrid(OBSERVE_GRID_1, OBSERVE_GRID_1_RADIUS, 1, OBSERVE_GRID_1_RADIUS));
            record(record);
            log.info(
                    "received: keys=[{}], distance={}, grid={}",
                    record.getKeys().stream().collect(Collectors.joining(",")),
                    String.format(Locale.US, "%.03f", distance),
                    record.getGrid()
            );
            if (distance <= tol) {
                log.info("Reached!");
                System.exit(0);
            }
        }
        return worldState;
    }

    private WorldState stepReplay() throws Exception {
        if (Objects.isNull(network)) {
            network = new GsonBuilder().create().fromJson(new FileReader("tmp/0.3406.9.network.json"), SimpleLayeredNeuralNetwork.class);
        }
        final WorldState worldState = getAgentHost().peekWorldState();
        Thread.sleep(50);
        final TimestampedStringVector observations = worldState.getObservations();
        for (int i = 0; i < observations.size(); i++) {
            final TimestampedString o = worldState.getObservations().get(i);
            final TimestampedStringWrapper ow = new TimestampedStringWrapper(o);
            double[] output = network.thinkOutput(new double[][]{normalizeGrid(ow.getGrid(OBSERVE_GRID_1, OBSERVE_GRID_1_RADIUS, 1, OBSERVE_GRID_1_RADIUS))})[0];

            int max = 0;
            for (int j = 0; j < output.length; j++) {
                if (output[j] > output[max]) {
                    max = j;
                }
            }
            if (max == 0) {
                getAgentHost().sendCommand("move 0.5");
            } else {
                getAgentHost().sendCommand("move 0");
            }
            if (max == 1) {
                getAgentHost().sendCommand("strafe -0.5");
            } else if (max == 2) {
                getAgentHost().sendCommand("strafe 0.5");
            } else {
                getAgentHost().sendCommand("strafe 0");
            }
        }
        return worldState;
    }

    private WorldState stepReplayDL4J() throws Exception {
//        ComputationGraph computationGraph = ModelSerializer.restoreComputationGraph(multiLayerNetworkPath);
        final WorldState worldState = getAgentHost().peekWorldState();

        final TimestampedStringVector observations = worldState.getObservations();
        for (int i = 0; i < observations.size(); i++) {
            final TimestampedString o = worldState.getObservations().get(i);
            final TimestampedStringWrapper ow = new TimestampedStringWrapper(o);
            final double[] grid = normalizeGrid(ow.getGrid(OBSERVE_GRID_1, OBSERVE_GRID_1_RADIUS, 1, OBSERVE_GRID_1_RADIUS));

            final INDArray input = Nd4j.zeros(OBSERVE_GRID_1_RADIUS * OBSERVE_GRID_1_RADIUS);
            for (int j = 0; j < grid.length; j++) {
                input.putScalar(j, grid[j] == 1 ? 1 : 0);
            }

            INDArray output = multiLayerNetwork.output(input);
            System.out.println(output);
            if (output.getDouble(0) > 0.6) {
                getAgentHost().sendCommand("strafe 0");
                getAgentHost().sendCommand("move 0.75");
            } else {
                getAgentHost().sendCommand("move 0");
                if (output.getDouble(1) > output.getDouble(2)) {
                    getAgentHost().sendCommand("strafe -0.75");
                } else {
                    getAgentHost().sendCommand("strafe 0.75");
                }
            }

        }

        Thread.sleep(500);
        return worldState;
    }

    private void initDL4J() throws IOException {
        int layerNum = 0;
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .iterations(2500)
                .useDropConnect(true)
                .weightInit(WeightInit.XAVIER)
                .activation(Activation.RELU)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .biasInit(1)
                .miniBatch(true)
                .learningRate(0.1)
                .list()
                .layer(layerNum++,
                        new DenseLayer.Builder()
                                .nIn(OBSERVE_GRID_1_RADIUS * OBSERVE_GRID_1_RADIUS)
                                .nOut(50)
                                .activation(Activation.SIGMOID)
                                .weightInit(WeightInit.DISTRIBUTION)
                                .dist(new UniformDistribution(0, 1))
                                .build()
                )
                .layer(layerNum++,
                        new DenseLayer.Builder()
                                .nIn(50)
                                .nOut(30)
                                .activation(Activation.SIGMOID)
                                .weightInit(WeightInit.DISTRIBUTION)
                                .dist(new UniformDistribution(0, 1))
                                .build()
                )
                .layer(layerNum++,
                        new DenseLayer.Builder()
                                .nIn(30)
                                .nOut(18)
                                .activation(Activation.SIGMOID)
                                .weightInit(WeightInit.DISTRIBUTION)
                                .dist(new UniformDistribution(0, 1))
                                .build()
                )
                .layer(layerNum++,
                        new DenseLayer.Builder()
                                .nIn(18)
                                .nOut(9)
                                .activation(Activation.SIGMOID)
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

        Lava1Mission.Record[] rawRecords = new GsonBuilder().create().fromJson(new FileReader("record/last.json"), Lava1Mission.Record[].class);

        Lava1Mission.Record[] records = Arrays.stream(rawRecords)
                .filter(record -> !record.getKeys().isEmpty())
                .filter(record -> record.getKeys().size() == 1)
                .toArray(Record[]::new);

        INDArray input = Nd4j.zeros(records.length, OBSERVE_GRID_1_RADIUS * OBSERVE_GRID_1_RADIUS);
        INDArray output = Nd4j.zeros(records.length, 3);

        for (int i = 0; i < records.length; i++) {
            final Record record = records[i];
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
        System.out.println(result);

        Evaluation eval = new Evaluation(3);
        eval.eval(ds.getLabels(), result);
        System.out.println(eval.stats());

        ModelSerializer.writeModel(multiLayerNetwork, multiLayerNetworkPath, false);
    }

    @Override
    protected AgentHost initAgentHost() {
        return new AgentHost();
    }

    @Override
    protected MissionSpec initMissionSpec() {
        MissionSpec missionSpec = new MissionSpec();
        missionSpec.timeLimitInSeconds(60);

        TerrainGen.generator.setSeed(666);
//        final Pair<IntPoint3D, IntPoint3D> p = TerrainGen.emptyRoomWithTransverseObstacles(missionSpec, 55, 150, 1, "lava", 0);
        final Pair<IntPoint3D, IntPoint3D> p = TerrainGen.emptyRoomWithTransverseObstacles(missionSpec, 55, 200, 1, "dirt", 1);
//        final Pair<IntPoint3D, IntPoint3D> p = TerrainGen.maze(missionSpec, 21, 50);

        final int r = Math.floorDiv(OBSERVE_GRID_1_RADIUS, 2);
        missionSpec.observeGrid(-r, -1, -r, r, -1, r, OBSERVE_GRID_1);
        missionSpec.observeDistance(p.getRight().x + 0.5f, p.getRight().y + 1, p.getRight().z + 0.5f, OBSERVE_DISTANCE_1);
        missionSpec.startAt(p.getLeft().x, p.getLeft().y, p.getLeft().z);
        missionSpec.setTimeOfDay(12000, false);

        return missionSpec;
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    @Override
    protected MissionRecordSpec initMissionRecordSpec() {
        MissionRecordSpec missionRecordSpec = new MissionRecordSpec("./record/lava1mission.tgz");
//        missionRecordSpec.recordMP4(20, 400000);
//        missionRecordSpec.recordCommands();
//        missionRecordSpec.recordRewards();
//        missionRecordSpec.recordObservations();
        return missionRecordSpec;
    }

    @Override
    protected InputOutputBundle getTrainingSetFromRecord(List<Record> recordList) {
        Set<Record> recordSet = recordList.stream().filter(record -> !record.getKeys().isEmpty()).distinct().collect(Collectors.toSet());

        Map<String[][][], Map<Collection<String>, Integer>> map = new TreeMap<>((o1, o2) -> {
            for (int i = 0; i < o1.length; i++) {
                for (int i1 = 0; i1 < o1[i].length; i1++) {
                    for (int i2 = 0; i2 < o1[i][i1].length; i2++) {
                        if (!o1[i][i1][i2].equalsIgnoreCase(o2[i][i1][i2])) {
                            return 1;
                        }
                    }
                }
            }
            return 0;
        });

        for (Record record : recordSet) {
            final String[][][] grid = record.getGrid();
            final Collection<String> keys = record.getKeys();
            Map<Collection<String>, Integer> m = map.get(grid);
            if (Objects.isNull(m)) {
                m = new TreeMap<>((o1, o2) -> {
                    Set<String> a = new TreeSet<>(String::compareToIgnoreCase);
                    a.addAll(o1);
                    Set<String> b = new TreeSet<>(String::compareToIgnoreCase);
                    b.addAll(o2);
                    return (a.size() == b.size() && a.containsAll(b)) ? 0 : 1;
                });
                map.put(grid, m);
            }
            m.putIfAbsent(keys, 0);
            m.put(keys, m.get(keys) + 1);
        }

        recordSet.clear();
        map.forEach((strings, collectionIntegerMap) -> recordSet.add(new Record(
                collectionIntegerMap.entrySet().stream().max(Comparator.comparing(Map.Entry::getValue)).get().getKey(),
                strings
        )));

        final double[][] input = new double[recordSet.size()][];
        final double[][] output = new double[recordSet.size()][];

        int i = 0;
        for (Record record : recordSet) {
            input[i] = normalizeGrid(record.getGrid());
            output[i] = new double[]{
                    record.getKeys().contains("W") ? 1 : 0,
                    record.getKeys().contains("A") ? 1 : 0,
                    record.getKeys().contains("D") ? 1 : 0
            };
            i++;
        }

        return new InputOutputBundle(
                new String[]{
                        "Grid: 0=All, 1=Lava",
                        "Key list: W A/D"
                },
                input,
                output
        );
    }

    @Override
    protected Type getRecordListTypeToken() {
        return new TypeToken<List<Record>>() {
        }.getType();
    }

    private double[] normalizeGrid(String[][][] grid) {
        final double[] t = new double[OBSERVE_GRID_1_RADIUS * OBSERVE_GRID_1_RADIUS];
        int i = 0;
        for (String[] strings : grid[0]) {
            for (String string : strings) {
                t[i++] = string.equalsIgnoreCase("stone") ? 0 : 1;
            }
        }
        return t;
    }

    @SuppressWarnings("WeakerAccess")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Record {
        private Collection<String> keys;
        private String[][][] grid;
    }

}
