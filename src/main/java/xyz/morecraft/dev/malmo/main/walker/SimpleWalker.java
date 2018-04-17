package xyz.morecraft.dev.malmo.main.walker;

import com.microsoft.msr.malmo.AgentHost;
import com.microsoft.msr.malmo.WorldState;
import xyz.morecraft.dev.malmo.proto.Mission;
import xyz.morecraft.dev.malmo.proto.MissionRunner;
import xyz.morecraft.dev.malmo.util.Blocks;
import xyz.morecraft.dev.malmo.util.GridVisualizer;
import xyz.morecraft.dev.malmo.util.WorldObservation;

import java.util.Objects;

public abstract class SimpleWalker<T extends Mission<?>> implements MissionRunner<T> {

    protected GridVisualizer gridVisualizer = new GridVisualizer(true, true);

    @Override
    public int stepInterval() {
        return 250;
    }

    @Override
    public WorldState step(AgentHost agentHost, WorldState worldState, WorldObservation worldObservation, T mission) throws Exception {
        if (Objects.isNull(worldObservation)) {
            return worldState;
        }

        final double speedMove = 0.65;
        final double speedStrafe = 0.65;

        switch (calculateNextStep(worldObservation, mission)) {
            case 0:
                agentHost.sendCommand("move " + speedMove);
                agentHost.sendCommand("strafe 0");
                break;
            case 1:
                agentHost.sendCommand("move 0");
                agentHost.sendCommand("strafe " + speedStrafe);
                break;
            case 2:
                agentHost.sendCommand("move -" + speedMove);
                agentHost.sendCommand("strafe 0");
                break;
            case 3:
                agentHost.sendCommand("move 0");
                agentHost.sendCommand("strafe -" + speedStrafe);
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
     * @param worldObservation Current observations object (not null)
     * @return Calculated direction.
     */
    public abstract int calculateNextStep(final WorldObservation worldObservation, Mission<?> mission);

    protected static boolean[][] toBooleanGrid(final String[][] rawGrid, int r) {
        final boolean[][] b = new boolean[r][r];
        for (int i = 0; i < r; i++) {
            for (int j = 0; j < r; j++) {
                b[i][j] = Blocks.isWalkable(rawGrid[i][j]);
            }
        }
        return b;
    }

}
