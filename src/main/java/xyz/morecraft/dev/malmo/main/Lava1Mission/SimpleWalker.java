package xyz.morecraft.dev.malmo.main.Lava1Mission;

import com.microsoft.msr.malmo.AgentHost;
import com.microsoft.msr.malmo.WorldState;
import lombok.extern.slf4j.Slf4j;
import xyz.morecraft.dev.malmo.mission.Lava1Mission;
import xyz.morecraft.dev.malmo.proto.MissionRunner;
import xyz.morecraft.dev.malmo.util.GridVisualizer;
import xyz.morecraft.dev.malmo.util.IntPoint3D;
import xyz.morecraft.dev.malmo.util.WayUtils;
import xyz.morecraft.dev.malmo.util.WorldObservation;

import java.util.Arrays;
import java.util.Objects;

import static xyz.morecraft.dev.malmo.util.BlockNames.BLOCK_STONE;

@Slf4j
public class SimpleWalker implements MissionRunner<Lava1Mission> {

    private GridVisualizer gridVisualizer = new GridVisualizer(true, true);

    public SimpleWalker() {

    }

    @Override
    public int stepInterval() {
        return 250;
    }

    @Override
    public WorldState step(AgentHost agentHost, Lava1Mission mission) throws Exception {
        final WorldState worldState = agentHost.peekWorldState();
        final WorldObservation worldObservation = WorldObservation.fromWorldState(worldState);

        if (Objects.isNull(worldObservation)) {
            return worldState;
        }

        final String[][][] rawRawGrid = worldObservation.getGrid(Lava1Mission.OBSERVE_GRID_0, Lava1Mission.OBSERVE_GRID_0_RADIUS, 1, Lava1Mission.OBSERVE_GRID_0_RADIUS);
        final String[][] rawGrid = WayUtils.revertGrid(rawRawGrid[0], Lava1Mission.OBSERVE_GRID_0_RADIUS);

        final IntPoint3D currentPoint = worldObservation.getPos();
        final IntPoint3D destinationPoint = mission.getP().getRight();

        switch (calculateNextStep(rawGrid, currentPoint, destinationPoint)) {
            case 0:
                agentHost.sendCommand("move 0.5");
                agentHost.sendCommand("strafe 0");
                break;
            case 1:
                agentHost.sendCommand("move 0");
                agentHost.sendCommand("strafe 0.4");
                break;
            case 2:
                agentHost.sendCommand("move -0.5");
                agentHost.sendCommand("strafe 0");
                break;
            case 3:
                agentHost.sendCommand("move 0");
                agentHost.sendCommand("strafe -0.4");
                break;
            default:
                break;
        }

        return worldState;
    }

    /**
     * Directions:
     * <pre>
     *      0
     *  3       1
     *      2
     * </pre>
     *
     * @param rawGrid          Raw 2d grid fetched from WorldObservation.
     * @param currentPoint     Player location.
     * @param destinationPoint Destination location.
     * @return Calculated direction.
     */
    public int calculateNextStep(final String[][] rawGrid, final IntPoint3D currentPoint, final IntPoint3D destinationPoint) {
        final boolean[][] grid = toBooleanGrid(rawGrid, Lava1Mission.OBSERVE_GRID_0_RADIUS);

        gridVisualizer.updateGrid(rawGrid);

        final double angle = WayUtils.getAngle(currentPoint.x, currentPoint.z, destinationPoint.x, destinationPoint.z);

        final int goalDirection = (Math.abs((int) Math.floor(angle / 90.0 + 0.5)) + 3) % 4;
        final int[][] transform = new int[][]{
                {0, 1},
                {1, 2},
                {2, 1},
                {1, 0}
        };

        int goDirection = 0;
        for (int i = 0; i < 4; i++) {
            final int j = (i + goalDirection) % 4;
            final int[] p = transform[j];
            if (grid[p[0]][p[1]]) {
                goDirection = j;
                break;
            }
        }

        log.info("angle={}, goalDirection={}, goDirection={}, grid={}", (int) angle, goalDirection, goDirection, Arrays.deepToString(grid));

        return goDirection;
    }

    private boolean[][] toBooleanGrid(final String[][] rawGrid, int r) {
        final boolean[][] b = new boolean[r][r];
        for (int i = 0; i < r; i++) {
            for (int j = 0; j < r; j++) {
                b[i][j] = BLOCK_STONE.equalsIgnoreCase(rawGrid[i][j]);
            }
        }
        return b;
    }

}
