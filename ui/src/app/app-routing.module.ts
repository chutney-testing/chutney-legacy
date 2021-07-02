import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { LoginComponent } from '@core/components/login/login.component';
import { ParentComponent } from '@core/components/parent/parent.component';
import { AuthGuard } from '@core/guards';
import { Authorization } from '@model';

export const appRoutes: Routes = [
    { path: 'login', component: LoginComponent },
    { path: 'login/:action', component: LoginComponent },
    {
        path: '', component: ParentComponent,
        children: [
            {
                path: 'home-page',
                loadChildren: './modules/home-page/home-page.module#HomePageModule'
            },
            {
                path: 'scenario',
                loadChildren: './modules/scenarios/scenario.module#ScenarioModule',
                canActivate: [AuthGuard],
                data: { 'authorizations': [ Authorization.SCENARIO_READ,Authorization.SCENARIO_WRITE,Authorization.SCENARIO_EXECUTE ] }
            },
            {
                path: 'campaign',
                loadChildren: './modules/campaign/campaign.module#CampaignModule',
                canActivate: [AuthGuard],
                data: { 'authorizations': [ Authorization.CAMPAIGN_READ,Authorization.CAMPAIGN_WRITE,Authorization.CAMPAIGN_EXECUTE ] }
            },
            {
                path: 'component',
                loadChildren: './modules/component/component.module#ComponentModule',
                canActivate: [AuthGuard],
                data: { 'authorizations': [ Authorization.COMPONENT_READ,Authorization.COMPONENT_WRITE ] }
            },
            {
                path: 'variable',
                loadChildren: './modules/global-variable/global-variable.module#GlobalVariableModule',
                canActivate: [AuthGuard],
                data: { 'authorizations': [ Authorization.GLOBAL_VAR_READ,Authorization.GLOBAL_VAR_WRITE ] }
            },
            {
                path: 'dataset',
                loadChildren: './modules/dataset/dataset.module#DatasetModule',
                canActivate: [AuthGuard],
                data: { 'authorizations': [ Authorization.DATASET_READ,Authorization.DATASET_WRITE ] }
            },
            {
                path: 'documentation',
                loadChildren: './organisms/documentation/documentation.module#DocumentationModule'
            },
            {
                path: 'configurationAgent',
                loadChildren: './modules/agent-network/agent-network.module#AgentNetworkModule',
                canActivate: [AuthGuard],
                data: { 'authorizations': [ Authorization.ADMIN_ACCESS ] }
            },
            {
                path: 'plugins',
                loadChildren: './modules/plugins/plugin-configuration.module#PluginConfigurationModule',
                canActivate: [AuthGuard],
                data: { 'authorizations': [ Authorization.ADMIN_ACCESS ] }
            },
            {
                path: 'databaseAdmin',
                loadChildren: './modules/database-admin/database-admin.module#DatabaseAdminModule',
                canActivate: [AuthGuard],
                data: { 'authorizations': [ Authorization.ADMIN_ACCESS ] }
            },
            {
                path: 'environmentAdmin',
                loadChildren: './organisms/environment-admin/environment-admin.module#EnvironmentAdminModule',
                canActivate: [AuthGuard],
                data: { 'authorizations': [ Authorization.ENVIRONMENT_ACCESS,Authorization.ADMIN_ACCESS ] }
            },
            {
                path: 'storesAdmin',
                loadChildren: './modules/stores-admin/stores-admin.module#StoresAdminModule',
                canActivate: [AuthGuard],
                data: { 'authorizations': [ Authorization.ADMIN_ACCESS ] }
            },
            {
                path: 'backups',
                loadChildren: './modules/backups/backups.module#BackupsModule',
                canActivate: [AuthGuard],
                data: { 'authorizations': [ Authorization.ADMIN_ACCESS ] }
            },
            {
                path: 'roles',
                loadChildren: './modules/roles/roles.module#RolesModule',
                canActivate: [AuthGuard],
                data: { 'authorizations': [ Authorization.ADMIN_ACCESS ] }
            },
        ]
    }
];

@NgModule({
    imports: [RouterModule.forRoot(appRoutes, { useHash: true, enableTracing: false })],
    exports: [RouterModule]
})
export class AppRoutingModule { }
