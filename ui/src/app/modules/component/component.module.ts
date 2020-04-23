// Core
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
// External libs
import { MomentModule } from 'angular2-moment';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { TranslateModule } from '@ngx-translate/core';
// Internal common
import { SharedModule } from '@shared/shared.module';
import { MoleculesModule } from 'src/app/molecules/molecules.module';

import { componentRoute } from './component.routes';
import { CreateComponent } from './components/create-component/create-component.component';
import { DragulaModule } from 'ng2-dragula';
import { ActionEditComponent } from './components/action/action-edit.component';
import { StrategyFormComponent } from '@modules/component/components/sub/strategy-form/strategy-form.component';

import { ParametersComponent } from './components/sub/parameters/parameters.component';

import { ExecutionPanelComponent } from './components/sub/execution-panel/execution-panel.component';
import { ChildPanelComponent } from './components/sub/child-panel/child-panel.component';
import { ToolbarComponent } from './components/sub/toolbar/toolbar.component';
import { StrategyParameterFormComponent } from '@modules/component/components/sub/strategy-form/parameter-form/strategy-parameter-form.component';

const ROUTES = [
    ...componentRoute
];

@NgModule({
    imports: [
        // Core
        CommonModule,
        RouterModule.forChild(ROUTES),
        FormsModule,
        ReactiveFormsModule,

        // External libs
        MomentModule,
        NgbModule,
        TranslateModule,
        DragulaModule,
        // Internal common
        SharedModule,
        MoleculesModule
    ],
    declarations: [
        CreateComponent,
        ActionEditComponent,
        StrategyFormComponent,
        StrategyParameterFormComponent,
        ParametersComponent,
        ToolbarComponent,
        ExecutionPanelComponent,
        ChildPanelComponent
    ],
})
export class ComponentModule {
}
