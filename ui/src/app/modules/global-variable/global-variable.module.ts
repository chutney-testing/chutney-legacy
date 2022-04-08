import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MomentModule } from 'angular2-moment';

import { NgbModule } from '@ng-bootstrap/ng-bootstrap';

import { TranslateModule } from '@ngx-translate/core';

import { SharedModule } from '@shared/shared.module';

import { GlobalVariableEditionComponent } from './components/global-variable-edition/global-variable-edition.component';
import { GlobalVariableRoute } from './global-variable.routes';
import { MoleculesModule } from '../../molecules/molecules.module';


const ROUTES = [
    ...GlobalVariableRoute
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
        MoleculesModule
    ],
    declarations: [
        GlobalVariableEditionComponent
    ]
})
export class GlobalVariableModule {
}
