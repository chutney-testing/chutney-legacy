import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { TranslateModule } from '@ngx-translate/core';
import { MoleculesModule } from '../../molecules/molecules.module';
import { BackupsAdminComponent } from './components/backups-admin.component';
import { BackupsAdminRoute } from '@modules/backups/backups.routes';
import { SharedModule } from '@shared/shared.module';

@NgModule({
    imports: [
        CommonModule,
        RouterModule.forChild(BackupsAdminRoute),
        FormsModule,
        ReactiveFormsModule,
        TranslateModule,
        MoleculesModule,
        SharedModule
    ],
    declarations: [BackupsAdminComponent],
})
export class BackupsModule {
}
