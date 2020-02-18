import { AgentInfo, TargetId } from '.';

export class AgentGraphe {
    constructor(
        public agents: Array<Agent>,
    ) { }
}

export class Agent {
    constructor(
        public info: AgentInfo,
        public reachableAgents: Array<string>,
        public reachableTargets: Array<TargetId>,
    ) { }

    public reachableSize() {
        return this.reachableAgents.length + this.reachableTargets.length;
    }
}
