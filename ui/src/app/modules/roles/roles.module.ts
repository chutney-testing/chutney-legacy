import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';

import { RolesRoute } from './roles.routes';

import { SharedModule } from '@shared/shared.module';
import { MoleculesModule } from '../../molecules/molecules.module';
import { RolesComponent } from './components/roles.component';

@NgModule({
  imports: [
    CommonModule,
    RouterModule.forChild(RolesRoute),
    FormsModule,
    TranslateModule,
    MoleculesModule,
    SharedModule
  ],
  declarations: [RolesComponent],
})
export class RolesModule {
}
