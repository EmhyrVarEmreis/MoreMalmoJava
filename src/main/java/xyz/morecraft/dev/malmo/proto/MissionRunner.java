package xyz.morecraft.dev.malmo.proto;

import com.microsoft.msr.malmo.AgentHost;
import com.microsoft.msr.malmo.WorldState;

public interface MissionRunner<T extends Mission<?>> {

    int stepInterval();

    WorldState step(AgentHost agentHost, T mission) throws Exception;

}
