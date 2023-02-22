import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { DateFormatPipe, MomentModule } from 'ngx-moment';

import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { TranslateModule } from '@ngx-translate/core';
import { AngularMultiSelectModule } from 'angular2-multiselect-dropdown';
import { NgChartsModule } from 'ng2-charts';
import { DragulaModule } from 'ng2-dragula';

import { SharedModule } from '@shared/shared.module';
import { MoleculesModule } from '../../molecules/molecules.module';
import { CampaignRoute } from './campaign.routes';
import { CampaignListComponent } from './components/campaign-list/campaign-list.component';
import { CampaignSchedulingComponent } from './components/campaign-scheduling/campaign-scheduling.component';
import { CampaignEditionComponent } from './components/create-campaign/campaign-edition.component';
import { CampaignExecutionComponent } from './components/execution/detail/campaign-execution.component';
import { CampaignExecutionsHistoryComponent } from './components/execution/history/campaign-executions-history.component';
import { CampaignExecutionsComponent } from './components/execution/history/list/campaign-executions.component';
import { CampaignExecutionMenuComponent } from './components/execution/sub/right-side-bar/campaign-execution-menu.component';

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
        NgChartsModule,
        MoleculesModule
    ],
    declarations: [
        CampaignListComponent,
        CampaignEditionComponent,
        CampaignExecutionsComponent,
        CampaignExecutionComponent,
        CampaignSchedulingComponent,
        CampaignExecutionsHistoryComponent,
        CampaignExecutionMenuComponent
    ],
    providers: [
        DateFormatPipe
    ]
})
export class CampaignModule {
}
