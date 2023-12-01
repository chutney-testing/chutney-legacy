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

import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';

import { AgentNetworkRoute } from './agent-network.routes';

import { AgentComponent } from './components/agent/agent.component';
import { AgentNetworkComponent } from './components/agent-network/agent-network.component';
import { NetworkGraphComponent } from './components/network-graph/network-graph.component';
import { AgentNetworkConfigurationComponent } from './components/agent-network-configuration/agent-network-configuration.component';
import { SharedModule } from '@shared/shared.module';

@NgModule({
    imports: [
        CommonModule,
        RouterModule.forChild(AgentNetworkRoute),
        FormsModule,
        TranslateModule,
        SharedModule
    ],
    declarations: [AgentNetworkComponent, AgentComponent, NetworkGraphComponent, AgentNetworkConfigurationComponent],
})
export class AgentNetworkModule {
}
