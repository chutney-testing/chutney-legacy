import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';

import { DatabaseAdminRoute } from './database-admin.routes';

import { MoleculesModule } from '../../molecules/molecules.module';
import { DatabaseAdminComponent } from './components/database-admin.component';

@NgModule({
  imports: [
    CommonModule,
    RouterModule.forChild(DatabaseAdminRoute),
    FormsModule,
    TranslateModule,
    MoleculesModule
  ],
  declarations: [DatabaseAdminComponent],
})
export class DatabaseAdminModule {
}
