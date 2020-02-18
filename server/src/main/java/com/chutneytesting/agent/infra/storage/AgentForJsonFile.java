package com.chutneytesting.agent.infra.storage;

import java.util.List;

class AgentForJsonFile {
    String host, name;
    int port;
    List<String> reachableAgentNames;
    List<TargetForJsonFile> reachableTargetIds;
}
