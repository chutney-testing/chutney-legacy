import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ScenariosComponent } from './containers/scenarios/scenarios.component';
import { UiCommonsModule } from '@chutney/ui-commons';
import { UiMaterialModule } from '@chutney/ui-material';
import { ScenariosListComponent } from './components/scearios-list/scenarios-list.component';

@NgModule({
  imports: [
    CommonModule,
    UiCommonsModule,
    RouterModule.forChild([{ path: '', component: ScenariosComponent }]),
    UiMaterialModule,
  ],
  declarations: [ScenariosComponent, ScenariosListComponent],
})
export class FeatureScenariosModule {}
