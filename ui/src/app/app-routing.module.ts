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
import { RouterModule, Routes } from '@angular/router';

import { LoginComponent } from '@core/components/login/login.component';
import { AuthGuard } from '@core/guards';
import { Authorization } from '@model';
import { ParentComponent } from '@core/components/parent/parent.component';
import { ChutneyMainHeaderComponent } from '@shared/components/layout/header/chutney-main-header.component';
import { ChutneyLeftMenuComponent } from '@shared/components/layout/left-menu/chutney-left-menu.component';
import { FeatureName } from '@core/feature/feature.model';
import { FeaturesGuard } from '@core/guards/features.guard';
import { FeaturesResolver } from '@core/feature/features.resolver';

export const appRoutes: Routes = [
    {path: 'login', component: LoginComponent},
    {path: 'login/:action', component: LoginComponent},
    {
        path: '', component: ParentComponent,
        canActivate: [AuthGuard],
        resolve: {'features': FeaturesResolver},
        children: [
            {path: '', component: ChutneyMainHeaderComponent, outlet: 'header'},
            {path: '', component: ChutneyLeftMenuComponent, outlet: 'left-side-bar'},
            {path: '', redirectTo: '/scenario', pathMatch: 'full'},
            {
                path: 'scenario',
                loadChildren: () => import('./modules/scenarios/scenario.module').then(m => m.ScenarioModule),
                canActivate: [AuthGuard],
                data: {'authorizations': [Authorization.SCENARIO_READ, Authorization.SCENARIO_WRITE, Authorization.SCENARIO_EXECUTE]}
            },
            {
                path: 'campaign',
                loadChildren: () => import('./modules/campaign/campaign.module').then(m => m.CampaignModule),
                canActivate: [AuthGuard],
                data: {'authorizations': [Authorization.CAMPAIGN_READ, Authorization.CAMPAIGN_WRITE, Authorization.CAMPAIGN_EXECUTE]}
            },
            {
                path: 'variable',
                loadChildren: () => import('./modules/global-variable/global-variable.module').then(m => m.GlobalVariableModule),
                canActivate: [AuthGuard],
                data: {'authorizations': [Authorization.GLOBAL_VAR_READ, Authorization.GLOBAL_VAR_WRITE]}
            },
            {
                path: 'dataset',
                loadChildren: () => import('./modules/dataset/dataset.module').then(m => m.DatasetModule),
                canActivate: [AuthGuard, FeaturesGuard], // add requiredAuthorizations
                data: {
                    'authorizations': [Authorization.DATASET_READ, Authorization.DATASET_WRITE]
                }
            },
            {
                path: 'configurationAgent',
                loadChildren: () => import('./modules/agent-network/agent-network.module').then(m => m.AgentNetworkModule),
                canActivate: [AuthGuard],
                data: {'authorizations': [Authorization.ADMIN_ACCESS]}
            },
            {
                path: 'plugins',
                loadChildren: () => import('./modules/plugins/plugin-configuration.module').then(m => m.PluginConfigurationModule),
                canActivate: [AuthGuard],
                data: {'authorizations': [Authorization.ADMIN_ACCESS]}
            },
            {
                path: 'databaseAdmin',
                loadChildren: () => import('./modules/database-admin/database-admin.module').then(m => m.DatabaseAdminModule),
                canActivate: [AuthGuard],
                data: {'authorizations': [Authorization.ADMIN_ACCESS]}
            },
            {
                path: 'targets',
                loadChildren: () => import('./modules/target/target.module').then(m => m.TargetModule),
                canActivate: [AuthGuard],
                data: {'authorizations': [Authorization.ENVIRONMENT_ACCESS, Authorization.ADMIN_ACCESS]}
            },
            {
                path: 'environments',
                loadChildren: () => import('./modules/environment/environment.module').then(m => m.EnvironmentModule),
                canActivate: [AuthGuard],
                data: {'authorizations': [Authorization.ENVIRONMENT_ACCESS, Authorization.ADMIN_ACCESS]}
            },
            {
                path: 'environmentsVariables',
                loadChildren: () => import('./modules/environment-variable/environment-variable.module').then(m => m.EnvironmentVariableModule),
                canActivate: [AuthGuard],
                data: {'authorizations': [Authorization.ENVIRONMENT_ACCESS, Authorization.ADMIN_ACCESS]}
            },
            {
                path: 'backups',
                loadChildren: () => import('./modules/backups/backups.module').then(m => m.BackupsModule),
                canActivate: [AuthGuard],
                data: {'authorizations': [Authorization.ADMIN_ACCESS]}
            },
            {
                path: 'roles',
                loadChildren: () => import('./modules/roles/roles.module').then(m => m.RolesModule),
                canActivate: [AuthGuard],
                data: {'authorizations': [Authorization.ADMIN_ACCESS]}
            },
            {
                path: 'metrics',
                loadChildren: () => import('./modules/metrics/metrics.module').then(m => m.MetricsModule),
                canActivate: [AuthGuard],
                data: {'authorizations': [Authorization.ADMIN_ACCESS]}
            }
        ]
    },
    {path: '**', redirectTo: ''}

];

@NgModule({
    imports: [RouterModule.forRoot(appRoutes, {useHash: true, enableTracing: false, relativeLinkResolution: 'legacy'})],
    exports: [RouterModule]
})
export class AppRoutingModule {
}
