// Core
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
// External libs
import { MomentModule } from 'angular2-moment';
import { NgbModule, NgbDropdownModule } from '@ng-bootstrap/ng-bootstrap';
import { FileSaverModule } from 'ngx-filesaver';
import { AngularMultiSelectModule } from 'angular2-multiselect-dropdown';
import { TranslateModule } from '@ngx-translate/core';
import {
    PerfectScrollbarModule,
    PerfectScrollbarConfigInterface,
    PERFECT_SCROLLBAR_CONFIG
} from 'ngx-perfect-scrollbar';
// Internal common
import { SharedModule } from '@shared/shared.module';
// Internal
import { scenarioRoute } from './scenario.routes';
import { ScenariiComponent } from './components/scenarii/scenarii.component';
import { StepReportComponent } from './components/execution/step-report/step-report.component';
import { StepComponent } from './components/execution/step/step.component';
import { HistoryComponent } from './components/execution/history/history.component';
import { ScenarioExecutionComponent } from './components/execution/execution.component';
import { MoleculesModule } from '../../molecules/molecules.module';
import { EditionComponent } from './components/edition/scenario-edition/edition.component';
import { RawEditionComponent } from './components/edition/raw/raw-edition.component';
import { ComponentEditionComponent } from './components/edition/component-edition/component-edition.component';
import { DragulaModule } from 'ng2-dragula';
import { ExecuteComponent } from './components/execute/execute.component';
import { HeaderComponent } from './components/sub/header/header.component';
import { ScenarioCampaignsComponent } from '@modules/scenarios/components/sub/scenario-campaigns/scenario-campaigns.component';

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
        NgbDropdownModule
    ],
    declarations: [
        ScenariiComponent,
        ScenarioExecutionComponent,
        StepReportComponent,
        StepComponent,
        ScenarioCampaignsComponent,
        HistoryComponent,
        EditionComponent,
        RawEditionComponent,
        ComponentEditionComponent,
        ExecuteComponent,
        HeaderComponent,
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
