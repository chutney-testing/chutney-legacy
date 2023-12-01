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
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { TranslateModule } from '@ngx-translate/core';
import { MoleculesModule } from '../../molecules/molecules.module';
import { BackupsAdminComponent } from './components/backups-admin.component';
import { BackupsAdminRoute } from '@modules/backups/backups.routes';
import { SharedModule } from '@shared/shared.module';

@NgModule({
    imports: [
        CommonModule,
        RouterModule.forChild(BackupsAdminRoute),
        FormsModule,
        ReactiveFormsModule,
        TranslateModule,
        MoleculesModule,
        SharedModule
    ],
    declarations: [BackupsAdminComponent],
})
export class BackupsModule {
}
