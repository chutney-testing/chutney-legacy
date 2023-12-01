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

import { DatasetListComponent } from './components/dataset-list/dataset-list.component';
import { DatasetEditionComponent } from './components/dataset-edition/dataset-edition.component';
import { CanDeactivateGuard, AuthGuard } from '@core/guards';
import { Authorization } from '@model';

export const DatasetRoute: Routes = [
    {
        path: '',
        component: DatasetListComponent,
        canActivate: [AuthGuard],
        data: { 'authorizations': [ Authorization.DATASET_READ,Authorization.DATASET_WRITE ] }
    },
    {
        path: ':id/edition',
        component: DatasetEditionComponent,
        canDeactivate: [CanDeactivateGuard],
        canActivate: [AuthGuard],
        data: { 'authorizations': [ Authorization.DATASET_WRITE ] }
    },
    {
        path: 'edition',
        component: DatasetEditionComponent,
        canDeactivate: [CanDeactivateGuard],
        canActivate: [AuthGuard],
        data: { 'authorizations': [ Authorization.DATASET_WRITE ] }
    }
];
