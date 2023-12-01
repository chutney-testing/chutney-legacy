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

import { Component, Input } from '@angular/core';
import { AgentGraphe, Agent, Environment } from '@model';
import { EnvironmentService } from '@core/services';

@Component({
  selector: 'chutney-network-graph',
  templateUrl: './network-graph.component.html',
  styleUrls: ['./network-graph.component.scss']
})
export class NetworkGraphComponent {

  agentNodes: Array<Agent>;

  environments: Array<Environment> = [];
  targetReachByAgent = new Map<string, Array<string>>();
  targetFilter = '';

  public constructor( private environmentAdminService: EnvironmentService) {
  }

  @Input() message: string;
  @Input()
  set agentGraphe(agentGraphe: AgentGraphe) {
    this.loadDescription(agentGraphe);
    this.loadUnreachableTarget();
  }

  private loadDescription(agentGraphe: AgentGraphe): void {
    this.agentNodes = agentGraphe.agents;
  }

  loadUnreachableTarget() {
    this.environmentAdminService.list().subscribe(
        (res) => {
            this.environments = res.sort((t1, t2) =>  t1.name.toUpperCase() > t2.name.toUpperCase() ? 1 : 0);
            this.targetReachByAgent = new Map<string, Array<string>>();

            this.agentNodes.forEach(agent => {
                agent.reachableTargets.forEach(target => {
                  if (this.targetReachByAgent.has(target.name)) {
                    this.targetReachByAgent.get(target.name).push(agent.info.name);
                  } else {
                    this.targetReachByAgent.set(target.name, [agent.info.name]);
                  }
                });
            });
        },
        (error) =>  console.log(error)
    );

}

}
