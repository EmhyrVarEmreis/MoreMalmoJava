package xyz.morecraft.dev.malmo.main.walker;

import com.microsoft.msr.malmo.AgentHost;
import com.microsoft.msr.malmo.WorldState;
import xyz.morecraft.dev.malmo.proto.MissionRunner;
import xyz.morecraft.dev.malmo.proto.MissionWithObserveGrid;
import xyz.morecraft.dev.malmo.util.Blocks;
import xyz.morecraft.dev.malmo.util.GridVisualizer;
import xyz.morecraft.dev.malmo.util.WorldObservation;

import java.util.Objects;

public abstract class SimpleWalker<T extends MissionWithObserveGrid<?>> implements MissionRunner<T> {

    protected GridVisualizer gridVisualizer = new GridVisualizer(true, true);

    private final static double speedMove = 1;
    private final static double speedStrafe = speedMove;
    private final static double pitchYawTol0 = 0.05;
    private final static double pitchYawTol1 = -1 * pitchYawTol0;
    private boolean isContinuous;

    public SimpleWalker() {
        this.isContinuous = true;
    }

    @Override
    public int stepInterval() {
        return 20;
    }

    @Override
    public void prepare(T mission) {
    }

    @Override
    public WorldState step(AgentHost agentHost, WorldState worldState, WorldObservation worldObservation, T mission) {
        if (Objects.isNull(worldObservation)) {
            return worldState;
        }

        final double yaw = worldObservation.getYaw();
        if (yaw > pitchYawTol0 || yaw < pitchYawTol1) {
            agentHost.sendCommand("turn " + (-1 * yaw / 150));
        } else {
            agentHost.sendCommand("turn 0");
        }

        final int goDirection = calculateNextStep(worldObservation, mission);
        if (isContinuous) {
            sendContinuousCommands(agentHost, goDirection);
        } else {
            sendNonContinuousCommands(agentHost, goDirection);
        }

        return worldState;
    }

    private void sendContinuousCommands(final AgentHost agentHost, final int goDirection) {
        switch (goDirection) {
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
    }

    private void sendNonContinuousCommands(final AgentHost agentHost, final int goDirection) {
        switch (goDirection) {
            case 0:
                agentHost.sendCommand("movenorth");
                break;
            case 1:
                agentHost.sendCommand("moveeast");
                break;
            case 2:
                agentHost.sendCommand("movesouth");
                break;
            case 3:
                agentHost.sendCommand("movewest");
                break;
            default:
                break;
        }
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
    public abstract int calculateNextStep(final WorldObservation worldObservation, MissionWithObserveGrid<?> mission);

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
