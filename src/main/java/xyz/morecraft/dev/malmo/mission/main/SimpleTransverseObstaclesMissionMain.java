package xyz.morecraft.dev.malmo.mission.main;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import xyz.morecraft.dev.malmo.main.simpleTransverseObstacles.Neural;
import xyz.morecraft.dev.malmo.main.simpleTransverseObstacles.Recorder;
import xyz.morecraft.dev.malmo.main.simpleTransverseObstacles.SimpleNeural;
import xyz.morecraft.dev.malmo.main.walker.impl.*;
import xyz.morecraft.dev.malmo.mission.SimpleTransverseObstaclesMission;
import xyz.morecraft.dev.malmo.proto.Mission;
import xyz.morecraft.dev.malmo.proto.MissionRunner;
import xyz.morecraft.dev.malmo.proto.MissionWithObserveGrid;
import xyz.morecraft.dev.malmo.util.Util;
import xyz.morecraft.dev.malmo.util.WorldObservation;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.util.*;
import java.util.function.Supplier;

import static xyz.morecraft.dev.malmo.proto.Mission.MAP_GRID_NAME;
import static xyz.morecraft.dev.malmo.proto.Mission.MAP_GRID_RADIUS;

@Slf4j
public class SimpleTransverseObstaclesMissionMain {

    public static void main(String[] args) throws Exception {
        Util.ensureMalmoXsdPath();
        Util.loadMalmoLib();

        Locale.setDefault(Locale.US);

        final Map<Integer, Pair<String, Supplier<MissionRunner<SimpleTransverseObstaclesMission>>>> map = new HashMap<>();

        map.put(0, new ImmutablePair<>("Recorder", Recorder::new));
        map.put(1, new ImmutablePair<>("Neural", Neural::new));
        map.put(2, new ImmutablePair<>("SimpleNeural", SimpleNeural::new));
        map.put(3, new ImmutablePair<>("P1", SimpleWalkerV1::new)); // P1
        map.put(4, new ImmutablePair<>("P2", SimpleWalkerV2::new)); // P2
        map.put(5, new ImmutablePair<>("P3", SimpleWalkerV3::new)); // P3
        map.put(6, new ImmutablePair<>("R1", SimpleWalkerB1::new)); // R1
        map.put(7, new ImmutablePair<>("D1", SimpleWalkerB2::new)); // D1
        map.put(8, new ImmutablePair<>("D2", SimpleWalkerB3::new)); // D2

//        final int n = 1;
        final int n = 50;
//        final int[] conf = {3};
        final int[] conf = {3, 4, 5, 6, 7, 8};

        final String recordName = System.currentTimeMillis() + "";

        FileWriter fileWriter = new FileWriter("record/" + recordName + ".txt", false);

        String[][] mapGrid = null;
        for (int idx : conf) {
            for (int i = 0; i < n; i++) {
                final Pair<String, Supplier<MissionRunner<SimpleTransverseObstaclesMission>>> stringSupplierPair = map.get(idx);
                final SimpleTransverseObstaclesMission simpleTransverseObstaclesMission = new SimpleTransverseObstaclesMission(args, 1);
                final Mission.MissionResult result = simpleTransverseObstaclesMission.run(stringSupplierPair.getRight().get());
                if (Objects.isNull(result)) {
                    log.error("Iteration {} resulted with null", i);
                    continue;
                }
                fileWriter.write(stringSupplierPair.getLeft());
                fileWriter.write(';');
                fileWriter.write(String.format("%,4f", result.getRunningTime()));
                fileWriter.write(';');
                fileWriter.write(String.format("%,4f", result.getReward()));
                fileWriter.write(';');
                fileWriter.write("" + getBlockCount(result, simpleTransverseObstaclesMission));
                fileWriter.write('\n');
                fileWriter.flush();
                if (Objects.isNull(mapGrid)) {
                    mapGrid = getMapGrid(result);
                }
                Thread.sleep(1000);
            }
        }
        fileWriter.close();
        saveMapGrid(mapGrid, "record/" + recordName + ".png");
    }

    private static int getBlockCount(final Mission.MissionResult result, final MissionWithObserveGrid missionWithObserveGrid) {
        int n = 0;
        String[][] prev = null;
        for (final WorldObservation worldObservation : result.getWorldObservationCollection()) {
            final String[][] curr = Optional.ofNullable(worldObservation.getGrid(missionWithObserveGrid.getDefaultObserveGridName(), 10, 1, 10)).orElse(new String[][][]{null})[0];
            if (Objects.isNull(curr)) {
                continue;
            }
            if (Objects.isNull(prev) || !Arrays.deepEquals(curr, prev)) {
                n++;
            }
            prev = curr;
        }
        return n;
    }

    private static String[][] getMapGrid(final Mission.MissionResult result) {
        final WorldObservation worldObservation = result.getWorldObservationCollection().stream().reduce((first, second) -> second).orElse(null);
        if (Objects.nonNull(worldObservation)) {
            final String[][][] mapGridTmp = worldObservation.getGrid(MAP_GRID_NAME, MAP_GRID_RADIUS * 2 + 1, 1, MAP_GRID_RADIUS * 2 + 1);
            if (Objects.nonNull(mapGridTmp)) {
                return mapGridTmp[0];
            }
        }
        return null;
    }

    private static void saveMapGrid(final String[][] grid, final String filePath) throws Exception {
        if (Objects.isNull(grid)) {
            return;
        }

        int i0 = Integer.MAX_VALUE;
        int i1 = Integer.MIN_VALUE;
        int j0 = Integer.MAX_VALUE;
        int j1 = Integer.MIN_VALUE;
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                if (grid[i][j].equalsIgnoreCase("dirt")) {
                    if (i < i0) {
                        i0 = i;
                    }
                    if (j < j0) {
                        j0 = j;
                    }
                    if (i > i1) {
                        i1 = i;
                    }
                    if (j > j1) {
                        j1 = j;
                    }
                }
            }
        }

        final int lineWidth = 3;
        final int blockWidth = 12;
        final int ni = i1 - i0 + 1;
        final int nj = j1 - j0 + 1;
        final BufferedImage bufferedImage = new BufferedImage(ni * blockWidth + (ni - 1) * lineWidth, nj * blockWidth + (nj - 1) * lineWidth, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = bufferedImage.createGraphics();

        for (int i = 0; i < ni; i++) {
            for (int j = 0; j < nj; j++) {
                final String blockName = grid[i0 + i][j0 + j];
                if (blockName.equalsIgnoreCase("stone")) {
                    g.setColor(Color.GRAY);
                } else if (blockName.equalsIgnoreCase("dirt")) {
                    g.setColor(Color.BLACK);
                } else if (blockName.equalsIgnoreCase("glowstone")) {
                    g.setColor(Color.ORANGE);
                } else if (blockName.equalsIgnoreCase("grass")) {
                    g.setColor(new Color(0, 100, 0));
                } else {
                    g.setColor(Color.WHITE);
                }
                //noinspection SuspiciousNameCombination
                g.fillRect(i * (blockWidth + lineWidth), j * (blockWidth + lineWidth), blockWidth, blockWidth);
            }
        }

        ImageIO.write(bufferedImage, "PNG", new File(filePath));
    }

}
