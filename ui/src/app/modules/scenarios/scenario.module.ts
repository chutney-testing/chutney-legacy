// Core
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
// External libs
import { MomentModule } from 'angular2-moment';
import { NgbDropdownModule, NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { FileSaverModule } from 'ngx-filesaver';
import { AngularMultiSelectModule } from 'angular2-multiselect-dropdown';
import { TranslateModule } from '@ngx-translate/core';
import {
    PERFECT_SCROLLBAR_CONFIG,
    PerfectScrollbarConfigInterface,
    PerfectScrollbarModule
} from 'ngx-perfect-scrollbar';
// Internal common
import { SharedModule } from '@shared/shared.module';
// Internal
import { scenarioRoute } from './scenario.routes';
import { ScenariosComponent } from './components/search-list/scenarios.component';
import { StepReportComponent } from './components/execution/step-report/step-report.component';
import { StepComponent } from './components/execution/step/step.component';
import { HistoryComponent } from './components/execution/history/history.component';
import { ScenarioExecutionComponent } from './components/execution/execution.component';
import { MoleculesModule } from '../../molecules/molecules.module';
import { RawEditionComponent } from './components/edition/raw/raw-edition.component';
import { ComponentEditionComponent } from './components/edition/component-edition/component-edition.component';
import { DragulaModule } from 'ng2-dragula';
import { ExecuteComponent } from './components/execute/execute.component';
import { HeaderComponent } from './components/sub/header/header.component';
import { ScenarioCampaignsComponent } from '@modules/scenarios/components/sub/scenario-campaigns/scenario-campaigns.component';
import { ComponentReadComponent } from './components/execution/component-read/component-read.component';
import { GwtReadComponent } from './components/execution/gwt-read/gwt-read.component';
import { AuthoringInfoComponent } from './components/edition/authoring-info/authoring-info.component';
import { EditionInfoComponent } from './components/edition/edition-info/edition-info.component';

const ROUTES = [
    ...scenarioRoute
];

const DEFAULT_PERFECT_SCROLLBAR_CONFIG: PerfectScrollbarConfigInterface = {
    wheelPropagation: true
};

@NgModule({
    imports: [
        // Core
        CommonModule,
        RouterModule.forChild(ROUTES),
        ReactiveFormsModule,
        MomentModule,
        FormsModule,
        // External libs
        NgbModule,
        AngularMultiSelectModule,
        TranslateModule,
        PerfectScrollbarModule,
        DragulaModule,
        FileSaverModule,
        // Internal common
        SharedModule,
        MoleculesModule,
        NgbDropdownModule,
    ],
    declarations: [
        ScenariosComponent,
        ScenarioExecutionComponent,
        StepReportComponent,
        StepComponent,
        ScenarioCampaignsComponent,
        HistoryComponent,
        RawEditionComponent,
        ComponentEditionComponent,
        ExecuteComponent,
        HeaderComponent,
        ComponentReadComponent,
        GwtReadComponent,
        AuthoringInfoComponent,
        EditionInfoComponent
    ],
    providers: [
        {
            provide: PERFECT_SCROLLBAR_CONFIG,
            useValue: DEFAULT_PERFECT_SCROLLBAR_CONFIG
        },
    ]
})
export class ScenarioModule {
}
