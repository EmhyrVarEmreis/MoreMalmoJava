package xyz.morecraft.dev.malmo.proto;

import com.microsoft.msr.malmo.AgentHost;
import com.microsoft.msr.malmo.WorldState;

public interface MissionRunner<T> {

    int stepInterval();

    WorldState step(AgentHost agentHost, Mission<T> mission) throws Exception;

}
