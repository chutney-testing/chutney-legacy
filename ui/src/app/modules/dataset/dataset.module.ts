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
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MomentModule } from 'ngx-moment';
import { ReactiveFormsModule } from '@angular/forms';

import { NgbModule } from '@ng-bootstrap/ng-bootstrap';

import { TranslateModule } from '@ngx-translate/core';

import { SharedModule } from '@shared/shared.module';

import { DatasetListComponent } from './components/dataset-list/dataset-list.component';
import { DatasetRoute } from './dataset.routes';
import { MoleculesModule } from 'src/app/molecules/molecules.module';
import { AngularMultiSelectModule } from 'angular2-multiselect-dropdown';
import { DatasetEditionComponent } from './components/dataset-edition/dataset-edition.component';
import { AtomsModule } from '../../atoms/atoms.module';
import { FormsKeyValueComponent } from '@modules/dataset/components/dataset-edition/forms-key-value/forms-key-value.component';
import { FormsDataGridComponent } from '@modules/dataset/components/dataset-edition/forms-data-grid/forms-data-grid.component';


const ROUTES = [
    ...DatasetRoute
];

@NgModule({
    imports: [
        CommonModule,
        RouterModule.forChild(ROUTES),
        FormsModule,
        ReactiveFormsModule,
        SharedModule,
        NgbModule,
        MomentModule,
        TranslateModule,
        MoleculesModule,
        AngularMultiSelectModule,
        AtomsModule
    ],
    declarations: [
        DatasetListComponent,
        DatasetEditionComponent,
        FormsKeyValueComponent,
        FormsDataGridComponent
    ]
})
export class DatasetModule {
}
