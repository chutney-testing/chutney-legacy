import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { CovalentCodeEditorModule } from '@covalent/code-editor';

import { UiCommonsModule } from '@chutney/ui-commons';
import { UiMaterialModule } from '@chutney/ui-material';
import { UtilsModule } from '@chutney/utils';

import { VariablesComponent } from './containers/variables/variables.component';

@NgModule({
  imports: [
    CommonModule,
    RouterModule.forChild([
      { path: '', component: VariablesComponent }
    ]),
    UiCommonsModule,
    UiMaterialModule,
    UtilsModule,
    CovalentCodeEditorModule,
  ],
  declarations: [VariablesComponent],
})
export class FeatureVariablesModule {}
