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
