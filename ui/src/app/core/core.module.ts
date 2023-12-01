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
import { SharedModule } from '@shared/shared.module';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { LoginComponent } from './components/login/login.component';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { ParentComponent } from './components/parent/parent.component';
import { EnvironmentsNamesResolver } from '@core/services/environments-names.resolver';
import { EnvironmentsResolver } from '@core/services/environments.resolver';

@NgModule({
    declarations: [
        LoginComponent,
        ParentComponent,
    ],
    imports: [
        CommonModule,
        FormsModule,
        HttpClientModule,
        RouterModule,
        SharedModule,
        TranslateModule
    ],
    providers: [EnvironmentsNamesResolver, EnvironmentsResolver]

})
export class CoreModule { }
