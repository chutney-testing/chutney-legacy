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

import { Authorization } from '@model';
import { TargetsComponent } from '@modules/target/list/targets.component';
import { TargetComponent } from '@modules/target/details/target.component';
import { TargetsResolver } from '@modules/target/resolver/targets-resolver.service';
import { EnvironmentsNamesResolver } from '@core/services/environments-names.resolver';
import { EnvironmentsComponent } from '@modules/environment/list/environments.component';
import { EnvironmentsResolver } from '@core/services/environments.resolver';

export const environmentsRoutes: Routes = [
    {
        path: '',
        component: EnvironmentsComponent,
        resolve: {environments: EnvironmentsResolver},
        data: { 'authorizations': [ Authorization.ENVIRONMENT_ACCESS ] }
    }
];
