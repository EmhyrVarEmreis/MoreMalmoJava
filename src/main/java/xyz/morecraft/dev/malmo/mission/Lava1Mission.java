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
import org.nd4j.linalg.api.ops.impl.indexaccum.IAMax;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.morecraft.dev.malmo.proto.Mission;
import xyz.morecraft.dev.malmo.util.*;
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
    private final static String OBSERVE_GRID_2 = "og2";
    private final static String OBSERVE_DISTANCE_1 = "End";
    private final static int OBSERVE_GRID_1_RADIUS = 7;
    private final static int OBSERVE_GRID_2_WIDTH = 5;
    private final static float tol = 0.25f;

    private final GlobalKeyListener globalKeyListener = new GlobalKeyListener();

    private SimpleLayeredNeuralNetwork network;

    private final static String multiLayerNetworkPath = "record/multiLayerNetwork.model";
    private MultiLayerNetwork multiLayerNetwork;

    private static boolean isRecord = false;
    private static boolean isDL4J = true;

    private GridVisualizer gridVisualizer;

    private List<Float> lastDistanceQueue;

    public Lava1Mission(String[] argv) throws IOException {
        super(argv);
        this.lastDistanceQueue = new ArrayList<>(10000);
        this.gridVisualizer = new GridVisualizer();
        this.gridVisualizer.setVisible(true);
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
            final WorldObservation ow = new WorldObservation(observations.get(i));
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
            final WorldObservation ow = new WorldObservation(worldState.getObservations().get(i));
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
            final TimestampedString o = observations.get(i);
            final WorldObservation ow = new WorldObservation(o);
            final String[][][] rawGrid = ow.getGrid(OBSERVE_GRID_1, OBSERVE_GRID_1_RADIUS, 1, OBSERVE_GRID_1_RADIUS);
            final String[] lineGrid = ow.getGrid(OBSERVE_GRID_2, OBSERVE_GRID_2_WIDTH * 2 + 1);
            final double[] grid = normalizeGrid(rawGrid);

            final INDArray input = Nd4j.zeros(OBSERVE_GRID_1_RADIUS * OBSERVE_GRID_1_RADIUS);
            for (int j = 0; j < grid.length; j++) {
                input.putScalar(j, grid[j] == 1 ? 1 : 0);
            }

            gridVisualizer.updateGrid(rawGrid);

            lastDistanceQueue.add(ow.getDistance(OBSERVE_DISTANCE_1));

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
                    getAgentHost().sendCommand("strafe 0");
                    getAgentHost().sendCommand("move 0.5");
                    break;
                case 1:
                    getAgentHost().sendCommand("move 0");
                    getAgentHost().sendCommand("strafe -0.5");
                    break;
                case 2:
                    getAgentHost().sendCommand("move 0");
                    getAgentHost().sendCommand("strafe 0.5");
                    break;
                default:
                    break;
            }
        }

        Thread.sleep(200);
        return worldState;
    }

    private void initDL4J() throws IOException {
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
                                .nIn(OBSERVE_GRID_1_RADIUS * OBSERVE_GRID_1_RADIUS)
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

        final Set<Lava1Mission.Record> treeSet = new TreeSet<>((o11, o22) -> compareGrids(o11.getGrid(), o22.getGrid()));
        treeSet.addAll(recordsAll);
        final Lava1Mission.Record[] records = treeSet.toArray(new Lava1Mission.Record[treeSet.size()]);

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

    private static int compareGrids(String[][][] o1, String[][][] o2) {
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
    }

    @Override
    protected AgentHost initAgentHost() {
        return new AgentHost();
    }

    @Override
    protected MissionSpec initMissionSpec() {
        MissionSpec missionSpec = new MissionSpec();
        missionSpec.timeLimitInSeconds(600);

        TerrainGen.generator.setSeed(666);
//        final Pair<IntPoint3D, IntPoint3D> p = TerrainGen.emptyRoomWithTransverseObstacles(missionSpec, 55, 150, 1, "lava", 0);
        final Pair<IntPoint3D, IntPoint3D> p = TerrainGen.emptyRoomWithTransverseObstacles(missionSpec, 105, 300, 1, "dirt", 1);
//        final Pair<IntPoint3D, IntPoint3D> p = TerrainGen.maze(missionSpec, 21, 50);

        final int r = Math.floorDiv(OBSERVE_GRID_1_RADIUS, 2);
        missionSpec.observeGrid(-r, -1, -r, r, -1, r, OBSERVE_GRID_1);
        missionSpec.observeGrid(-OBSERVE_GRID_2_WIDTH, -1, 1, OBSERVE_GRID_2_WIDTH, -1, 1, OBSERVE_GRID_2);
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

        Map<String[][][], Map<Collection<String>, Integer>> map = new TreeMap<>(Lava1Mission::compareGrids);

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
