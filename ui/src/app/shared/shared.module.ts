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

import { CommonModule } from '@angular/common';
import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { DatasetSelectionComponent } from '@shared/components/dataset-selection/dataset-selection.component';
import { ThumbnailPipe } from '@shared/pipes/thumbnail.pipe';
import { MomentModule } from 'ngx-moment';
import { AlertService } from './alert.service';
import { ErrorInterceptor } from './error-interceptor.service';
import {
    EnvironmentComboComponent,
    ExecutionBadgeComponent
} from './components';
import {
    ImplementationHostDirective,
    HasAuthorizationDirective,
    InputFocusDirective,
    ResizeDirective
} from './directives';
import { EventManagerService } from './event-manager.service';
import {
    ComponentSearchPipe,
    DataSetSearchPipe,
    DurationPipe,
    LinkifyPipe,
    ObjectAsEntryListPipe,
    PrettyPrintPipe,
    SafePipe,
    ScenarioCampaignSearchPipe,
    ScenarioSearchPipe,
    SearchTextPipe,
    SortByFieldPipe,
    StringifyPipe,
    TruncatePipe,
    WithoutScenarioPipe
} from '@shared/pipes';
import { StateService } from './state/state.service';
import { ChutneyEditorComponent } from '@shared/components/chutney-editor/chutney-editor.component';
import { HjsonParserService } from '@shared/hjson-parser/hjson-parser.service';

import { PerfectScrollbarModule } from 'ngx-perfect-scrollbar';
import { NgbDropdownModule, NgbTooltipModule } from '@ng-bootstrap/ng-bootstrap';
import { ChutneyMainHeaderComponent } from '@shared/components/layout/header/chutney-main-header.component';
import { ChutneyLeftMenuComponent } from '@shared/components/layout/left-menu/chutney-left-menu.component';
import { ChutneyRightMenuComponent } from '@shared/components/layout/right-menu/chutney-right-menu.component';
import { DistinctPipe } from './pipes/distinct.pipe';

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        MomentModule,
        ReactiveFormsModule,
        RouterModule,
        TranslateModule,
        PerfectScrollbarModule,
        NgbTooltipModule,
        NgbDropdownModule
    ],
    declarations: [
        ComponentSearchPipe,
        DataSetSearchPipe,
        DatasetSelectionComponent,
        DurationPipe,
        LinkifyPipe,
        EnvironmentComboComponent,
        ExecutionBadgeComponent,
        ImplementationHostDirective,
        InputFocusDirective,
        ResizeDirective,
        ObjectAsEntryListPipe,
        PrettyPrintPipe,
        SafePipe,
        ScenarioCampaignSearchPipe,
        ScenarioSearchPipe,
        SearchTextPipe,
        SortByFieldPipe,
        StringifyPipe,
        ThumbnailPipe,
        TruncatePipe,
        WithoutScenarioPipe,
        HasAuthorizationDirective,
        ChutneyEditorComponent,
        ChutneyMainHeaderComponent,
        ChutneyLeftMenuComponent,
        ChutneyRightMenuComponent,
        DistinctPipe,
    ],
    exports: [
        ComponentSearchPipe,
        DataSetSearchPipe,
        DatasetSelectionComponent,
        DurationPipe,
        LinkifyPipe,
        EnvironmentComboComponent,
        ExecutionBadgeComponent,
        ImplementationHostDirective,
        InputFocusDirective,
        ResizeDirective,
        ObjectAsEntryListPipe,
        PrettyPrintPipe,
        SafePipe,
        ScenarioCampaignSearchPipe,
        ScenarioSearchPipe,
        SearchTextPipe,
        SortByFieldPipe,
        StringifyPipe,
        ThumbnailPipe,
        TruncatePipe,
        WithoutScenarioPipe,
        HasAuthorizationDirective,
        ChutneyEditorComponent,
        ChutneyRightMenuComponent,
        DistinctPipe,
    ],
    providers: [
        {
            provide: HTTP_INTERCEPTORS,
            useClass: ErrorInterceptor,
            multi: true
        },
        AlertService,
        EventManagerService,
        StateService,
        HjsonParserService
    ]
})
export class SharedModule {
}
