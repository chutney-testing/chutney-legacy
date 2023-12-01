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

import { PluginConfigurationRoute } from './plugin-configuration.routes';

import { MoleculesModule } from '../../molecules/molecules.module';
import { PluginConfigurationComponent } from './components/plugin-configuration.component';
import { JiraComponent } from '@modules/plugins/components/jira/jira.component';
import { LinkifierComponent } from '@modules/plugins/components/linkifier/linkifier.component';
import { SharedModule } from '@shared/shared.module';

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        MoleculesModule,
        ReactiveFormsModule,
        RouterModule.forChild(PluginConfigurationRoute),
        SharedModule,
        TranslateModule,
    ],
    declarations: [
        JiraComponent,
        LinkifierComponent,
        PluginConfigurationComponent
    ],
})
export class PluginConfigurationModule {
}
