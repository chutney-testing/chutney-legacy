import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { homePageRoute } from './home-page.routes';
import { SharedModule } from '@shared/shared.module';
import { NgbModalModule } from '@ng-bootstrap/ng-bootstrap';
import { CommonModule } from '@angular/common';
import { MoleculesModule } from '../../molecules/molecules.module';
import { HomePageCreateDialogComponent } from './components/home-page-create/home-page-create-dialog.component';
import { HomePageComponent } from './components/home-page/home-page.component';

@NgModule({

    imports: [
        RouterModule.forChild(homePageRoute),
        CommonModule,
        FormsModule,
        SharedModule,
        TranslateModule,
        NgbModalModule,
        MoleculesModule
    ],
    exports: [
        RouterModule
    ],
    declarations: [
        HomePageComponent,
        HomePageCreateDialogComponent
    ],
    entryComponents: [
        HomePageCreateDialogComponent
    ],
})
export class HomePageModule { }
