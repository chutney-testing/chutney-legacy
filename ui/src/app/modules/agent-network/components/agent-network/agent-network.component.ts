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
