import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';


import { MetricsComponent } from './components/metrics.component';
import { MetricsRoute } from './metrics.routes';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { MoleculesModule } from 'src/app/molecules/molecules.module';

@NgModule({
    imports: [
        CommonModule,
        NgbModule,
        TranslateModule,
        MoleculesModule,
        RouterModule.forChild(MetricsRoute)
    ],
    declarations: [
        MetricsComponent
    ],
})
export class MetricsModule {
}
