import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
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
      { path: 'text/add', component: VariablesTextEditComponent },
      { path: 'text/:groupName/edit', component: VariablesTextEditComponent }
    ]),
    UiCommonsModule,
    UiMaterialModule,
    UtilsModule,
    ReactiveFormsModule,
    CovalentCodeEditorModule,
    FormsModule,
  ],
  declarations: [
    VariablesComponent,
    VariablesListComponent,
    VariablesTextEditComponent
  ],
})
export class FeatureVariablesModule {}
