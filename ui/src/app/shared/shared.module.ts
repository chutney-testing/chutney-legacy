import { CommonModule } from '@angular/common';
import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { AceEditorComponent } from "@shared/ace-editor/ace-editor.component";
import { DatasetSelectionComponent } from '@shared/components/dataset-selection/dataset-selection.component';
import { InputFocusDirective } from '@shared/directives';
import { ThumbnailPipe } from '@shared/pipes/thumbnail.pipe';
import { MomentModule } from 'angular2-moment';
import { AceEditorModule } from "ng2-ace-editor";
import { AlertService } from './alert.service';
import { AuthInterceptor } from './AuthInterceptor';
import { ComponentCardComponent, EnvironmentComboComponent, ExecutionBadgeComponent, FunctionalStepComponent, ScenarioCardComponent, TechnicalStepComponent } from './components';
import { ImplementationHostDirective } from './directives';
import { EventManagerService } from './event-manager.service';
import { HjsonParserService } from './hjson-parser/hjson-parser.service';
import { ComponentSearchPipe, DurationPipe, ObjectAsEntryListPipe, PrettyPrintPipe, SafePipe, ScenarioCampaignSearchPipe, ScenarioSearchPipe, SearchTextPipe, SortByFieldPipe, StringifyPipe, TruncatePipe, WithoutScenarioPipe } from './pipes';
import { DataSetSearchPipe } from './pipes/dataset-search.pipe';
import { StateService } from './state/state.service';

@NgModule({
    imports: [
        CommonModule,
        RouterModule,
        TranslateModule,
        MomentModule,
        FormsModule,
        ReactiveFormsModule,
        AceEditorModule,
    ],
    declarations: [
        TruncatePipe,
        ObjectAsEntryListPipe,
        StringifyPipe,
        SearchTextPipe,
        ImplementationHostDirective,
        DurationPipe,
        PrettyPrintPipe,
        SortByFieldPipe,
        InputFocusDirective,
        SafePipe,
        ScenarioSearchPipe,
        ScenarioCampaignSearchPipe,
        ComponentSearchPipe,
        WithoutScenarioPipe,
        ScenarioCardComponent,
        TechnicalStepComponent,
        FunctionalStepComponent,
        EnvironmentComboComponent,
        ExecutionBadgeComponent,
        ThumbnailPipe,
        ComponentCardComponent,
        AceEditorComponent,
        DataSetSearchPipe,
        DatasetSelectionComponent,
    ],
    exports: [
        TruncatePipe,
        ObjectAsEntryListPipe,
        StringifyPipe,
        SearchTextPipe,
        ImplementationHostDirective,
        DurationPipe,
        PrettyPrintPipe,
        SortByFieldPipe,
        InputFocusDirective,
        SafePipe,
        ScenarioSearchPipe,
        ScenarioCampaignSearchPipe,
        ComponentSearchPipe,
        WithoutScenarioPipe,
        ScenarioCardComponent,
        TechnicalStepComponent,
        FunctionalStepComponent,
        EnvironmentComboComponent,
        ExecutionBadgeComponent,
        ThumbnailPipe,
        ComponentCardComponent,
        AceEditorComponent,
        DataSetSearchPipe,
        DatasetSelectionComponent,
    ],
    entryComponents: [
        TechnicalStepComponent
    ],
    providers: [
        {
            provide: HTTP_INTERCEPTORS,
            useClass: AuthInterceptor,
            multi: true
          },
        HjsonParserService,
        AlertService,
        EventManagerService,
        StateService]
})
export class SharedModule {
}
