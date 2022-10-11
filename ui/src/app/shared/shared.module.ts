import { CommonModule } from '@angular/common';
import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { DatasetSelectionComponent } from '@shared/components/dataset-selection/dataset-selection.component';
import { InputFocusDirective } from '@shared/directives';
import { ThumbnailPipe } from '@shared/pipes/thumbnail.pipe';
import { MomentModule } from 'ngx-moment';
import { AlertService } from './alert.service';
import { ErrorInterceptor } from './error-interceptor.service';
import {
    ComponentCardComponent,
    EnvironmentComboComponent,
    ExecutionBadgeComponent,
    ScenarioTreeComponent
} from './components';
import { ImplementationHostDirective, HasAuthorizationDirective } from './directives';
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
import { ChutneyMainHeaderComponent } from './components/chutney-main-header/chutney-main-header.component';
import { ChutneyLeftMenuComponent } from './components/chutney-left-menu/chutney-left-menu.component';
import { ChutneyRightMenuComponent } from './components/chutney-right-menu/chutney-right-menu.component';
import { PerfectScrollbarModule } from 'ngx-perfect-scrollbar';
import { NgbTooltipModule } from '@ng-bootstrap/ng-bootstrap';
import { Entry } from '@core/model';

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        MomentModule,
        ReactiveFormsModule,
        RouterModule,
        TranslateModule,
        PerfectScrollbarModule,
        NgbTooltipModule
    ],
    declarations: [
        ComponentCardComponent,
        ComponentSearchPipe,
        DataSetSearchPipe,
        DatasetSelectionComponent,
        DurationPipe,
        LinkifyPipe,
        EnvironmentComboComponent,
        ExecutionBadgeComponent,
        ImplementationHostDirective,
        InputFocusDirective,
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
        ScenarioTreeComponent
    ],
    exports: [
        ComponentCardComponent,
        ComponentSearchPipe,
        DataSetSearchPipe,
        DatasetSelectionComponent,
        DurationPipe,
        LinkifyPipe,
        EnvironmentComboComponent,
        ExecutionBadgeComponent,
        ImplementationHostDirective,
        InputFocusDirective,
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
        ScenarioTreeComponent
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
