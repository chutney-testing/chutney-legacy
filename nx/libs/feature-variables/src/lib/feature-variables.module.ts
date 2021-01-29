import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { VariablesComponent } from './containers/variables/variables.component';

@NgModule({
  imports: [
    CommonModule,
    RouterModule.forChild([
      { path: '', component: VariablesComponent }
    ])
  ],
  declarations: [VariablesComponent],
})
export class FeatureVariablesModule {}
