import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { StepRoutingModule } from './step-routing.module';
import { StepComponent } from './step.component';
import { TranslateModule } from '@ngx-translate/core';
import { MoleculesModule } from '../../molecules/molecules.module';
import { SharedModule } from '../../shared/shared.module';
import { SubstepComponent } from './substep/substep.component';
import { InfiniteScrollModule } from 'ngx-infinite-scroll';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    TranslateModule,
    MoleculesModule,
    StepRoutingModule,
    SharedModule,
    InfiniteScrollModule
  ],
  declarations: [
    StepComponent,
    SubstepComponent
  ]
})
export class StepModule { }
