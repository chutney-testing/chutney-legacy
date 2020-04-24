import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MomentModule } from 'angular2-moment';
import { ReactiveFormsModule } from '@angular/forms';

import { NgbModule } from '@ng-bootstrap/ng-bootstrap';

import { TranslateModule } from '@ngx-translate/core';

import { SharedModule } from '@shared/shared.module';

import { DatasetListComponent } from './components/dataset-list/dataset-list.component';
import { DatasetRoute } from './dataset.routes';
import { MoleculesModule } from 'src/app/molecules/molecules.module';
import { AngularMultiSelectModule } from 'angular2-multiselect-dropdown';
import { DatasetEditionComponent } from './components/dataset-edition/dataset-edition.component';


const ROUTES = [
    ...DatasetRoute
];

@NgModule({
    imports: [
        CommonModule,
        RouterModule.forChild(ROUTES),
        FormsModule,
        ReactiveFormsModule,
        SharedModule,
        NgbModule,
        MomentModule,
        TranslateModule,
        MoleculesModule,
        AngularMultiSelectModule
    ],
    declarations: [
        DatasetListComponent,
        DatasetEditionComponent
    ],
    entryComponents: [

    ],
})
export class DatasetModule {
}
