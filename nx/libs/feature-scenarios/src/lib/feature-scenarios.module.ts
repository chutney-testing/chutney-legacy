import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ScenariosComponent } from './containers/scenarios/scenarios.component';
import { UiCommonsModule } from '@chutney/ui-commons';
import { UiMaterialModule } from '@chutney/ui-material';
import { ScenariosListComponent } from './components/scenarios-list/scenarios-list.component';
import { ReactiveFormsModule } from '@angular/forms';
import { ScenariosSearchFormComponent } from './components/scenarios-search-form/scenarios-search-form.component';
import { ScenarioTextViewComponent } from './containers/scenario-text-view/scenario-text-view.component';
import { ScenarioTextRunComponent } from './containers/scenario-text-run/scenario-text-run.component';
import { DurationPipe } from './pipes/duration/duration.pipe';

@NgModule({
  imports: [
    CommonModule,
    UiCommonsModule,
    RouterModule.forChild([
      { path: '', component: ScenariosComponent },
      { path: 'text/:id/view', component: ScenarioTextViewComponent },
      {
        path: 'text/:id/run/:executionId',
        component: ScenarioTextRunComponent,
      },
    ]),
    UiMaterialModule,
    ReactiveFormsModule,
  ],
  declarations: [
    ScenariosComponent,
    ScenariosListComponent,
    ScenariosSearchFormComponent,
    ScenarioTextViewComponent,
    ScenarioTextRunComponent,
    DurationPipe,
  ],
})
export class FeatureScenariosModule {}
