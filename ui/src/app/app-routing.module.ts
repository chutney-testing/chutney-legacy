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
            { path: '', redirectTo: '/home', pathMatch: 'full' },
            {
                path: 'home',
                loadChildren: () => import('./modules/home-page/home-page.module').then(m => m.HomePageModule)
            },
            {
                path: 'scenario',
                loadChildren: () => import('./modules/scenarios/scenario.module').then(m => m.ScenarioModule),
                canActivate: [AuthGuard],
                data: { 'authorizations': [ Authorization.SCENARIO_READ,Authorization.SCENARIO_WRITE,Authorization.SCENARIO_EXECUTE ] }
            },
            {
                path: 'campaign',
                loadChildren: () => import('./modules/campaign/campaign.module').then(m => m.CampaignModule),
                canActivate: [AuthGuard],
                data: { 'authorizations': [ Authorization.CAMPAIGN_READ,Authorization.CAMPAIGN_WRITE,Authorization.CAMPAIGN_EXECUTE ] }
            },
            {
                path: 'component',
                loadChildren: () => import('./modules/component/component.module').then(m => m.ComponentModule),
                canActivate: [AuthGuard],
                data: { 'authorizations': [ Authorization.COMPONENT_READ,Authorization.COMPONENT_WRITE ] }
            },
            {
                path: 'variable',
                loadChildren: () => import('./modules/global-variable/global-variable.module').then(m => m.GlobalVariableModule),
                canActivate: [AuthGuard],
                data: { 'authorizations': [ Authorization.GLOBAL_VAR_READ,Authorization.GLOBAL_VAR_WRITE ] }
            },
            {
                path: 'dataset',
                loadChildren: () => import('./modules/dataset/dataset.module').then(m => m.DatasetModule),
                canActivate: [AuthGuard],
                data: { 'authorizations': [ Authorization.DATASET_READ,Authorization.DATASET_WRITE ] }
            },
            {
                path: 'documentation',
                loadChildren: () => import('./organisms/documentation/documentation.module').then(m => m.DocumentationModule)
            },
            {
                path: 'configurationAgent',
                loadChildren: () => import('./modules/agent-network/agent-network.module').then(m => m.AgentNetworkModule),
                canActivate: [AuthGuard],
                data: { 'authorizations': [ Authorization.ADMIN_ACCESS ] }
            },
            {
                path: 'plugins',
                loadChildren: () => import('./modules/plugins/plugin-configuration.module').then(m => m.PluginConfigurationModule),
                canActivate: [AuthGuard],
                data: { 'authorizations': [ Authorization.ADMIN_ACCESS ] }
            },
            {
                path: 'databaseAdmin',
                loadChildren: () => import('./modules/database-admin/database-admin.module').then(m => m.DatabaseAdminModule),
                canActivate: [AuthGuard],
                data: { 'authorizations': [ Authorization.ADMIN_ACCESS ] }
            },
            {
                path: 'environmentAdmin',
                loadChildren: () => import('./organisms/environment-admin/environment-admin.module').then(m => m.EnvironmentAdminModule),
                canActivate: [AuthGuard],
                data: { 'authorizations': [ Authorization.ENVIRONMENT_ACCESS,Authorization.ADMIN_ACCESS ] }
            },
            {
                path: 'backups',
                loadChildren: () => import('./modules/backups/backups.module').then(m => m.BackupsModule),
                canActivate: [AuthGuard],
                data: { 'authorizations': [ Authorization.ADMIN_ACCESS ] }
            },
            {
                path: 'roles',
                loadChildren: () => import('./modules/roles/roles.module').then(m => m.RolesModule),
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
