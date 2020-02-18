import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';

import { MainMenuComponent } from './main-menu/main-menu.component';
import { MoleculesModule } from '../molecules/molecules.module';

import { ProfileMenuComponent } from './profile-menu/profile-menu.component';

@NgModule({
    imports: [
        CommonModule,
        RouterModule,
        MoleculesModule,
        TranslateModule
    ],
    exports: [
        MainMenuComponent,
        ProfileMenuComponent,
    ],
    declarations: [
        MainMenuComponent,
        ProfileMenuComponent,
    ]
})
export class OrganismsModule {
}
