import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';

import { ShowroomRoute } from './showroom.routes';

import { AtomsModule } from '../../atoms/atoms.module';
import { ShowroomComponent } from './showroom.component';
import { MoleculesModule } from '../../molecules/molecules.module';
import { SharedModule } from '@shared/shared.module';

@NgModule({
  imports: [
    CommonModule,
    RouterModule.forChild(ShowroomRoute),
    TranslateModule,
    AtomsModule,
    MoleculesModule,
    SharedModule
  ],
  declarations: [ShowroomComponent]
})
export class ShowroomModule {
}
