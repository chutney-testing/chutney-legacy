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
import { ScenariosComponent } from './components/search-list/scenarios.component';
import { RawEditionComponent } from './components/edition/raw/raw-edition.component';
import { AuthGuard, CanDeactivateGuard } from '@core/guards';
import { Authorization } from '@model';
import {
    ScenarioExecutionsHistoryComponent
} from '@modules/scenarios/components/execution/history/scenario-executions-history.component';
import {
    ScenarioExecutionMenuComponent
} from '@modules/scenarios/components/execution/sub/right-side-bar/scenario-execution-menu.component';
import { FeaturesGuard } from '@core/guards/features.guard';
import { FeatureName } from '@core/feature/feature.model';
import { ReportPreviewComponent } from './components/execution/preview/report-preview.component';

export const scenarioRoute: Routes = [

    {
        path: '',
        component: ScenariosComponent,
        canActivate: [AuthGuard],
        data: {'authorizations': [Authorization.SCENARIO_READ]}
    },
    {
        path: ':id/executions',
        canActivate: [AuthGuard],
        data: {'authorizations': [Authorization.SCENARIO_READ]},
        children: [
            {
                path: '',
                component: ScenarioExecutionsHistoryComponent
            },
            {
                path: '',
                component: ScenarioExecutionMenuComponent,
                outlet: 'right-side-bar'
            },
        ]
    },
    {
        path: ':id/raw-edition',
        component: RawEditionComponent,
        canDeactivate: [CanDeactivateGuard],
        canActivate: [AuthGuard],
        data: {'authorizations': [Authorization.SCENARIO_WRITE]}
    },
    {
        path: 'raw-edition',
        component: RawEditionComponent,
        canDeactivate: [CanDeactivateGuard],
        canActivate: [AuthGuard],
        data: {'authorizations': [Authorization.SCENARIO_WRITE]}
    },
    {
        path: 'report-preview',
        component: ReportPreviewComponent,
        canActivate: [AuthGuard],
        data: {'authorizations': [Authorization.ADMIN_ACCESS]}
    }
];
