import { Component, Input } from '@angular/core';
import { AgentGraphe, Agent, EnvironmentMetadata } from '@model';
import { EnvironmentAdminService } from '@core/services';

@Component({
  selector: 'chutney-network-graph',
  templateUrl: './network-graph.component.html',
  styleUrls: ['./network-graph.component.scss']
})
export class NetworkGraphComponent {

  agentNodes: Array<Agent>;

  environments: Array<EnvironmentMetadata> = [];
  targetReachByAgent = new Map<string, Array<string>>();
  targetFilter = '';

  public constructor( private environmentAdminService: EnvironmentAdminService) {
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
    this.environmentAdminService.listEnvironments().subscribe(
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
