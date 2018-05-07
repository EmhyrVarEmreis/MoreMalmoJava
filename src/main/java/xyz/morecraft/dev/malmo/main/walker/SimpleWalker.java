package xyz.morecraft.dev.malmo.main.walker;

import com.microsoft.msr.malmo.AgentHost;
import com.microsoft.msr.malmo.WorldState;
import xyz.morecraft.dev.malmo.proto.MissionRunner;
import xyz.morecraft.dev.malmo.proto.MissionWithObserveGrid;
import xyz.morecraft.dev.malmo.util.Blocks;
import xyz.morecraft.dev.malmo.util.CardinalDirection;
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
        this(true);
    }

    public SimpleWalker(boolean isContinuous) {
        this.isContinuous = isContinuous;
    }

    @Override
    public int stepInterval() {
        return 20;
    }

    @Override
    public void prepare(T mission) {
        mission.setDefaultObserveGridRadius(getDefaultObserveGridRadius());
    }

    protected abstract int getDefaultObserveGridRadius();

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

        final CardinalDirection goDirection = calculateNextStep(worldObservation, mission);
        if (isContinuous) {
            sendContinuousCommands(agentHost, goDirection);
        } else {
            sendNonContinuousCommands(agentHost, goDirection);
        }

        return worldState;
    }

    private void sendContinuousCommands(final AgentHost agentHost, final CardinalDirection goDirection) {
        switch (goDirection) {
            case N:
                agentHost.sendCommand("move " + speedMove);
                agentHost.sendCommand("strafe 0");
                break;
            case E:
                agentHost.sendCommand("move 0");
                agentHost.sendCommand("strafe " + speedStrafe);
                break;
            case S:
                agentHost.sendCommand("move -" + speedMove);
                agentHost.sendCommand("strafe 0");
                break;
            case W:
                agentHost.sendCommand("move 0");
                agentHost.sendCommand("strafe -" + speedStrafe);
                break;
            default:
                break;
        }
    }

    private void sendNonContinuousCommands(final AgentHost agentHost, final CardinalDirection goDirection) {
        switch (goDirection) {
            case N:
                agentHost.sendCommand("movenorth");
                break;
            case E:
                agentHost.sendCommand("moveeast");
                break;
            case S:
                agentHost.sendCommand("movesouth");
                break;
            case W:
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
    public abstract CardinalDirection calculateNextStep(final WorldObservation worldObservation, MissionWithObserveGrid<?> mission);

    public static boolean[][] toBooleanGrid(final String[][] rawGrid, int r) {
        final boolean[][] b = new boolean[r][r];
        for (int i = 0; i < r; i++) {
            for (int j = 0; j < r; j++) {
                b[i][j] = Blocks.isWalkable(rawGrid[i][j]);
            }
        }
        return b;
    }

}
