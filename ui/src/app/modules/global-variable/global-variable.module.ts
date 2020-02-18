import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MomentModule } from 'angular2-moment';
import { ReactiveFormsModule } from '@angular/forms';

import { NgbModule } from '@ng-bootstrap/ng-bootstrap';

import { TranslateModule } from '@ngx-translate/core';

import { SharedModule } from '@shared/shared.module';

import { GlobalVariableEditionComponent } from './components/global-variable-edition/global-variable-edition.component';
import { GlobalVariableRoute } from './global-variable.routes';


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
        TranslateModule
    ],
    declarations: [
        GlobalVariableEditionComponent
    ],
    entryComponents: [

    ],
})
export class GlobalVariableModule {
}
