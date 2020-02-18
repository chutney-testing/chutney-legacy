import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';

import { SharedModule } from '@shared/shared.module';
import { AtomsModule } from '../../atoms/atoms.module';
import { MoleculesModule } from '../../molecules/molecules.module';

import { environmentAdminRoute } from './environment-admin.routes';
import { EnvironmentAdminComponent } from './environment-admin.component';
import { AddTargetDialogComponent } from './add-target-dialog/add-target-dialog.component';
import {FileSaverModule} from 'ngx-filesaver';

@NgModule({
    imports: [
        CommonModule,
        RouterModule.forChild(environmentAdminRoute),
        FormsModule,
        ReactiveFormsModule,
        SharedModule,
        AtomsModule,
        MoleculesModule,
        TranslateModule,
        FileSaverModule,
    ],
    declarations: [
        EnvironmentAdminComponent,
        AddTargetDialogComponent
    ],
})
export class EnvironmentAdminModule {
}
