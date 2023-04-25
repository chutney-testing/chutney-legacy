import { AgentNetworkComponent } from './components/agent-network/agent-network.component';
import { Routes } from '@angular/router';
import { Authorization } from '@model';

export const AgentNetworkRoute: Routes = [
    {
        path: '',
        component: AgentNetworkComponent,
        data: { 'authorizations': [ Authorization.ADMIN_ACCESS ] }
    }
];
