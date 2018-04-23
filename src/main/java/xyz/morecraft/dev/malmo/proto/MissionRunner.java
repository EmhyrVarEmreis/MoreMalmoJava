package xyz.morecraft.dev.malmo.proto;

import com.microsoft.msr.malmo.AgentHost;
import com.microsoft.msr.malmo.WorldState;
import xyz.morecraft.dev.malmo.util.WorldObservation;

public interface MissionRunner<T extends Mission<?>> {

    int stepInterval();

    void prepare(T mission) throws Exception;

    WorldState step(AgentHost agentHost, WorldState worldState, WorldObservation worldObservation, T mission) throws Exception;

}
