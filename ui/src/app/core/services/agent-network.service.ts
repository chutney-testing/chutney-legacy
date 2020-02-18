import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { NetworkConfiguration, AgentNetwork, AgentGraphe, Agent, TargetId } from '@model';
import { environment } from '@env/environment';
import { HttpClient } from '@angular/common/http';

@Injectable({
    providedIn: 'root'
})
export class AgentNetworkService {

    constructor(private http: HttpClient) { }

    sendAndSaveConfiguration(configuration: NetworkConfiguration): Observable<Object> {
        return this.http.post(environment.backend + '/api/v1/agentnetwork/configuration', configuration);
    }

    getDescription(): Observable<AgentNetwork> {
        return this.http.get(environment.backend + '/api/v1/description')
            .pipe(map((res: any) => new AgentNetwork(
                new AgentGraphe(res.agentsGraph.agents.map(this.buildAgent)),
                res.networkConfiguration as NetworkConfiguration)
            ));
    }

    /**
     * Build a real {@link Agent} object based on equivalent anonymous structure.
     * @param {Agent} agentDescription casted from anonymous object
     */
    private buildAgent = (agentDescription: Agent) => {
        return new Agent(agentDescription.info, agentDescription.reachableAgents, agentDescription.reachableTargets.map(this.buildTarget));
    }

    /**
     * Build a real {@link TargetId} object based on equivalent anonymous structure.
     * @param {TargetId} targetDescription casted from anonymous object
     */
    private buildTarget = (targetDescription: TargetId) => {
        return new TargetId(targetDescription.name);
    }
}
