import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { AuthGuard } from '@core/guards';
import { LoginComponent } from '@core/components/login/login.component';
import { ParentComponent } from '@core/components/parent/parent.component';

export const appRoutes: Routes = [
    {
        path: '', component: ParentComponent, canActivate: [AuthGuard], children: [
            { path: '', redirectTo: '/login', pathMatch: 'full' },
            { path: 'home-page', loadChildren: './modules/home-page/home-page.module#HomePageModule' },
            { path: 'scenario', loadChildren: './modules/scenarios/scenario.module#ScenarioModule' },
            { path: 'configurationAgent', loadChildren: './modules/agent-network/agent-network.module#AgentNetworkModule' },
            { path: 'campaign', loadChildren: './modules/campaign/campaign.module#CampaignModule' },
            { path: 'databaseAdmin', loadChildren: './modules/database-admin/database-admin.module#DatabaseAdminModule' },
            { path: 'environmentAdmin', loadChildren: './organisms/environment-admin/environment-admin.module#EnvironmentAdminModule' },
            { path: 'storesAdmin', loadChildren: './modules/stores-admin/stores-admin.module#StoresAdminModule' },
            { path: 'documentation', loadChildren: './organisms/documentation/documentation.module#DocumentationModule' },
            { path: 'showroom', loadChildren: './organisms/showroom/showroom.module#ShowroomModule' },
            { path: 'component', loadChildren: './modules/component/component.module#ComponentModule' },
            { path: 'variable', loadChildren: './modules/global-variable/global-variable.module#GlobalVariableModule' },
            { path: 'backups', loadChildren: './modules/backups/backups.module#BackupsModule' },
            { path: 'dataset', loadChildren: './modules/dataset/dataset.module#DatasetModule' },
        ]
    },
    { path: 'login', component: LoginComponent },
    { path: 'login/:action', component: LoginComponent }
];

@NgModule({
    imports: [RouterModule.forRoot(appRoutes, { useHash: true, enableTracing: false })],
    exports: [RouterModule]
})
export class AppRoutingModule { }
