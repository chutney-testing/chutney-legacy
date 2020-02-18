import { AgentGraphe, NetworkConfiguration } from '.';

export class AgentNetwork {
    constructor(
        readonly graphe: AgentGraphe,
        readonly networkConfiguration: NetworkConfiguration
    ) { }
}
