import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { CovalentCodeEditorModule } from '@covalent/code-editor';

import { UiCommonsModule } from '@chutney/ui-commons';
import { UiMaterialModule } from '@chutney/ui-material';
import { UtilsModule } from '@chutney/utils';

import { VariablesComponent } from './containers/variables/variables.component';
import { VariablesListComponent } from './components/variables-list/variables-list.component';
import { VariablesTextEditComponent } from './containers/variables-text-edit/variables-text-edit.component';

@NgModule({
  imports: [
    CommonModule,
    RouterModule.forChild([
      { path: '', component: VariablesComponent },
      { path: 'text/:groupName/edit', component: VariablesTextEditComponent }
    ]),
    UiCommonsModule,
    UiMaterialModule,
    UtilsModule,
    CovalentCodeEditorModule,
  ],
  declarations: [
    VariablesComponent,
    VariablesListComponent,
    VariablesTextEditComponent
  ],
})
export class FeatureVariablesModule {}
