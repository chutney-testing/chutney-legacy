import { AlertService } from './alert.service';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HjsonParserService } from './hjson-parser/hjson-parser.service';
import { InputFocusDirective } from '@shared/directives';
import { EventManagerService } from './event-manager.service';
import { StateService } from './state/state.service';
import {
    ScenarioSearchPipe,
    ComponentSearchPipe,
    TruncatePipe,
    ObjectAsEntryListPipe,
    StringifyPipe,
    SearchTextPipe,
    DurationPipe,
    PrettyPrintPipe,
    SortByFieldPipe,
    SafePipe,
    WithoutScenarioPipe,
    ScenarioCampaignSearchPipe
} from './pipes';
import { ImplementationHostDirective } from './directives';
import {
    ScenarioCardComponent,
    TechnicalStepComponent,
    FunctionalStepComponent,
    EnvironmentComboComponent,
    ExecutionBadgeComponent, ComponentCardComponent
} from './components';
import { RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { MomentModule } from 'angular2-moment';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ThumbnailPipe } from '@shared/pipes/thumbnail.pipe';
import {AceEditorComponent} from "@shared/ace-editor/ace-editor.component";
import {AceEditorModule} from "ng2-ace-editor";
import { DataSetSearchPipe } from './pipes/dataset-search.pipe';

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
        AceEditorComponent
        DataSetSearchPipe,
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
        AceEditorComponent
        DataSetSearchPipe,
    ],
    entryComponents: [
        TechnicalStepComponent
    ],
    providers: [HjsonParserService, AlertService, EventManagerService, StateService]
})
export class SharedModule {
}
