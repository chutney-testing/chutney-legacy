import { Component, OnInit } from '@angular/core';
import { AgentNetwork, NetworkConfiguration } from '@model';
import { AgentNetworkService } from '@core/services';

@Component({
  selector: 'chutney-agent-network',
  templateUrl: './agent-network.component.html',
  styleUrls: ['./agent-network.component.scss']
})
export class AgentNetworkComponent implements OnInit {

  currentConfiguration = new NetworkConfiguration([]);
  description: AgentNetwork;
  errorMessage: any;
  messages: string;

  constructor(
    private agentService: AgentNetworkService
  ) { }

  ngOnInit(): void {
    this.loadAll();
  }

  loadAll(): void {
    this.agentService.getDescription().subscribe(
      (description) => {this.description = description; },
      (error) => { this.messages = error.error; }
    );
  }

  propagationDone(message: string) {
    this.messages = message;
    this.loadAll();
  }

  loadDescription(description: AgentNetwork): void {
    this.description = description;
  }
}
