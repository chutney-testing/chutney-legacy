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
//TODO import { AceEditorModule } from 'ng2-ace-editor';
import { AlertService } from './alert.service';
import { ErrorInterceptor } from './error-interceptor.service';
import {
    ComponentCardComponent,
    EnvironmentComboComponent,
    ExecutionBadgeComponent
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

@NgModule({
    imports: [
       //TODO  AceEditorModule,
        CommonModule,
        FormsModule,
        MomentModule,
        ReactiveFormsModule,
        RouterModule,
        TranslateModule,
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
    ],
    providers: [
        {
            provide: HTTP_INTERCEPTORS,
            useClass: ErrorInterceptor,
            multi: true
        },
        AlertService,
        EventManagerService,
        StateService
    ]
})
export class SharedModule {
}
