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
import { CommonModule } from '@angular/common';
import { EnvironmentsComponent } from './list/environments.component';
import { RouterModule } from '@angular/router';
import { environmentsRoutes } from '@modules/environment/environments.routes';
import { MoleculesModule } from '../../molecules/molecules.module';
import { TranslateModule } from '@ngx-translate/core';
import { NgbTooltipModule } from '@ng-bootstrap/ng-bootstrap';


@NgModule({
    declarations: [
        EnvironmentsComponent
    ],
    imports: [
        CommonModule,
        RouterModule.forChild(environmentsRoutes),
        MoleculesModule,
        TranslateModule,
        NgbTooltipModule
    ]
})
export class EnvironmentModule {
}
