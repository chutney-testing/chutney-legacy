import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';

import { StoresAdminRoute } from './stores-admin.routes';

import { MoleculesModule } from '../../molecules/molecules.module';
import { StoresAdminComponent } from './components/stores-admin.component';

@NgModule({
  imports: [
    CommonModule,
    RouterModule.forChild(StoresAdminRoute),
    FormsModule,
    TranslateModule,
    MoleculesModule
  ],
  declarations: [StoresAdminComponent],
})
export class StoresAdminModule {
}
