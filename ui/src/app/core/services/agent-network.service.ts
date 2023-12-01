/**
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
