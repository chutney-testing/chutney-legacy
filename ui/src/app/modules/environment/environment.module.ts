import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EnvironmentsComponent } from './list/environments.component';
import { RouterModule } from '@angular/router';
import { environmentsRoutes } from '@modules/environment/environments.routes';
import { MoleculesModule } from '../../molecules/molecules.module';
import { TranslateModule } from '@ngx-translate/core';
import { NgbTooltipModule } from '@ng-bootstrap/ng-bootstrap';


@NgModule({
    declarations: [
        EnvironmentsComponent
    ],
    imports: [
        CommonModule,
        RouterModule.forChild(environmentsRoutes),
        MoleculesModule,
        TranslateModule,
        NgbTooltipModule
    ]
})
export class EnvironmentModule {
}
