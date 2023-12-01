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

import { Component, Input, Output, EventEmitter } from '@angular/core';
import { NetworkConfiguration, AgentInfo } from '@model';
import { AgentNetworkService } from '@core/services';


@Component({
    selector: 'chutney-agent-network-configuration',
    templateUrl: './agent-network-configuration.component.html',
    styleUrls: ['./agent-network-configuration.component.scss']
})
export class AgentNetworkConfigurationComponent {
    @Input() currentConfiguration: NetworkConfiguration;
    @Output() configurationUpdate = new EventEmitter();

    constructor(private agentService: AgentNetworkService) { }

    removeAgent(configurationAgent) {
        const index = this.currentConfiguration.agentNetworkConfiguration.indexOf(configurationAgent);
        this.currentConfiguration.agentNetworkConfiguration.splice(index, 1);
    }

    save() {
        this.configurationUpdate.emit('Propagation en cours');
        this.agentService.sendAndSaveConfiguration(this.currentConfiguration)
            .subscribe(
                (res) => this.configurationUpdate.emit('Propagation terminée'),
                (error) => this.configurationUpdate.emit('Erreur: ' + error));
    }

    addServer() {
        this.currentConfiguration.agentNetworkConfiguration.push(new AgentInfo('', '', 8350));
    }
}
