import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';

import { MoleculesModule } from '../molecules/molecules.module';
import { SharedModule } from '@shared/shared.module';


@NgModule({
    imports: [
        CommonModule,
        RouterModule,
        MoleculesModule,
        TranslateModule,
        SharedModule
    ],
    exports: [
    ],
    declarations: [
    ]
})
export class OrganismsModule {
}
