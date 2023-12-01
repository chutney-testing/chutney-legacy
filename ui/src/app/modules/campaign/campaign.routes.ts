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

import { Routes } from '@angular/router';

import { CampaignListComponent } from './components/campaign-list/campaign-list.component';
import { CampaignEditionComponent } from './components/create-campaign/campaign-edition.component';
import { CampaignSchedulingComponent } from './components/campaign-scheduling/campaign-scheduling.component';
import { CampaignExecutionsHistoryComponent } from './components/execution/history/campaign-executions-history.component';
import { CampaignExecutionMenuComponent } from './components/execution/sub/right-side-bar/campaign-execution-menu.component';
import { AuthGuard } from '@core/guards';
import { Authorization } from '@model';

export const CampaignRoute: Routes = [
    {
        path: '',
        component: CampaignListComponent,
        canActivate: [AuthGuard],
        data: { 'authorizations': [ Authorization.CAMPAIGN_READ ] }
    },
    {
        path: ':id/executions',
        canActivate: [AuthGuard],
        data: { 'authorizations': [ Authorization.CAMPAIGN_READ ] },
        children: [
            {
                path: '',
                component: CampaignExecutionsHistoryComponent
            },
            {
                path: '',
                component: CampaignExecutionMenuComponent,
                outlet: 'right-side-bar'
            }
        ]
    },
    {
        path: ':id/edition',component: CampaignEditionComponent,
        canActivate: [AuthGuard],
        data: { 'authorizations': [ Authorization.CAMPAIGN_WRITE ] }
    },
    {
        path: 'edition',
        component: CampaignEditionComponent,
        canActivate: [AuthGuard],
        data: { 'authorizations': [ Authorization.CAMPAIGN_WRITE ] }
    },
    {
        path: 'scheduling',
        component: CampaignSchedulingComponent,
        canActivate: [AuthGuard],
        data: { 'authorizations': [ Authorization.CAMPAIGN_EXECUTE ] }
    },
];
