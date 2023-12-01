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
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';

import { SharedModule } from '@shared/shared.module';
import { AtomsModule } from '../../atoms/atoms.module';
import { MoleculesModule } from '../../molecules/molecules.module';

import {FileSaverModule} from 'ngx-filesaver';
import { targetsRoutes } from '@modules/target/targets.routes';
import { TargetsComponent } from '@modules/target/list/targets.component';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { TargetComponent } from './details/target.component';
import { TargetsResolver } from '@modules/target/resolver/targets-resolver.service';
import { CoreModule } from '@core/core.module';
import { TooltipModule } from 'ngx-bootstrap/tooltip';

@NgModule({
    imports: [
        CommonModule,
        RouterModule.forChild(targetsRoutes),
        FormsModule,
        ReactiveFormsModule,
        SharedModule,
        CoreModule,
        AtomsModule,
        MoleculesModule,
        TranslateModule,
        FileSaverModule,
        NgbModule,
        TooltipModule
    ],
    declarations: [
        TargetsComponent,
        TargetComponent
    ],
    providers: [TargetsResolver]
})
export class TargetModule {
}
