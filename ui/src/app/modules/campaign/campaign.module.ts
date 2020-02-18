import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MomentModule } from 'angular2-moment';
import { ReactiveFormsModule } from '@angular/forms';

import { NgbModule } from '@ng-bootstrap/ng-bootstrap';

import { TranslateModule } from '@ngx-translate/core';

import { SharedModule } from '../../shared/shared.module';
import { CampaignRoute } from './campaign.routes';
import { CampaignListComponent } from './components/campaign-list/campaign-list.component';
import { CampaignExecutionComponent } from './components/execution/execution-campaign.component';
import { CampaignEditionComponent } from './components/create-campaign/campaign-edition.component';
import { DragulaModule } from 'ng2-dragula';
import { AngularMultiSelectModule } from 'angular2-multiselect-dropdown';

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
        AngularMultiSelectModule
    ],
    declarations: [
        CampaignListComponent,
        CampaignEditionComponent,
        CampaignExecutionComponent,
    ],
    entryComponents: [
        CampaignEditionComponent,
        CampaignExecutionComponent,
    ],
})
export class CampaignModule {
}
