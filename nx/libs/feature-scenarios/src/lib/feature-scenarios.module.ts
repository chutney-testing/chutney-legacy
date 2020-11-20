import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ScenariosComponent } from './containers/scenarios/scenarios.component';
import { UiCommonsModule } from '@chutney/ui-commons';
import { UiMaterialModule } from '@chutney/ui-material';
import { ScenariosListComponent } from './components/scenarios-list/scenarios-list.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ScenariosSearchFormComponent } from './components/scenarios-search-form/scenarios-search-form.component';
import { ScenarioTextViewComponent } from './containers/scenario-text-view/scenario-text-view.component';
import { ScenarioTextRunComponent } from './containers/scenario-text-run/scenario-text-run.component';
import { DurationPipe } from './pipes/duration/duration.pipe';
import { CovalentCodeEditorModule } from '@covalent/code-editor';
import { ScenarioTextEditComponent } from './containers/scenario-text-edit/scenario-text-edit.component';

@NgModule({
  imports: [
    CommonModule,
    UiCommonsModule,
    RouterModule.forChild([
      { path: '', component: ScenariosComponent },
      { path: 'text/:id/view', component: ScenarioTextViewComponent },
      { path: 'text/:id/edit', component: ScenarioTextEditComponent },
      {
        path: 'text/:id/run/:executionId',
        component: ScenarioTextRunComponent,
      },
    ]),
    UiMaterialModule,
    ReactiveFormsModule,
    CovalentCodeEditorModule,
    FormsModule,
  ],
  declarations: [
    ScenariosComponent,
    ScenariosListComponent,
    ScenariosSearchFormComponent,
    ScenarioTextViewComponent,
    ScenarioTextRunComponent,
    DurationPipe,
    ScenarioTextEditComponent,
  ],
})
export class FeatureScenariosModule {}
