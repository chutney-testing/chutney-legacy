import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MomentModule } from 'angular2-moment';
import { ReactiveFormsModule } from '@angular/forms';

import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { TranslateModule } from '@ngx-translate/core';
import { AngularMultiSelectModule } from 'angular2-multiselect-dropdown';
import { DragulaModule } from 'ng2-dragula';
import { ChartsModule } from 'ng2-charts';

import { SharedModule } from '@shared/shared.module';
import { CampaignRoute } from './campaign.routes';
import { CampaignListComponent } from './components/campaign-list/campaign-list.component';
import { CampaignExecutionComponent } from './components/execution/execution-campaign.component';
import { CampaignEditionComponent } from './components/create-campaign/campaign-edition.component';
import { CampaignSchedulingComponent } from './components/campaign-scheduling/campaign-scheduling.component';
import { MoleculesModule } from 'src/app/molecules/molecules.module';

const ROUTES = [
    ...CampaignRoute
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
        DragulaModule,
        AngularMultiSelectModule,
        ChartsModule,
        MoleculesModule
    ],
    declarations: [
        CampaignListComponent,
        CampaignEditionComponent,
        CampaignExecutionComponent,
        CampaignSchedulingComponent
    ],
    entryComponents: [
        CampaignEditionComponent,
        CampaignExecutionComponent,
    ],
})
export class CampaignModule {
}
