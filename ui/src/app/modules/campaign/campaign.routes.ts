import { Routes } from '@angular/router';

import { CampaignListComponent } from './components/campaign-list/campaign-list.component';
import { CampaignExecutionComponent } from './components/execution/execution-campaign.component';
import { CampaignEditionComponent } from './components/create-campaign/campaign-edition.component';
import { CampaignSchedulingComponent } from './components/campaign-scheduling/campaign-scheduling.component';
import { AuthGuard } from '@core/guards';
import { Authorization } from '@model';

export const CampaignRoute: Routes = [
    {
        path: '',
        component: CampaignListComponent,
        canActivate: [AuthGuard],
        data: { 'authorizations': [ Authorization.CAMPAIGN_READ ] }
    },
    {
        path: ':id/execution',
        component: CampaignExecutionComponent,
        canActivate: [AuthGuard],
        data: { 'authorizations': [ Authorization.CAMPAIGN_READ ] }
    },
    {
        path: ':id/edition',component: CampaignEditionComponent,
        canActivate: [AuthGuard],
        data: { 'authorizations': [ Authorization.CAMPAIGN_WRITE ] }
    },
    {
        path: 'edition',
        component: CampaignEditionComponent,
        canActivate: [AuthGuard],
        data: { 'authorizations': [ Authorization.CAMPAIGN_WRITE ] }
    },
    {
        path: 'scheduling',
        component: CampaignSchedulingComponent,
        canActivate: [AuthGuard],
        data: { 'authorizations': [ Authorization.CAMPAIGN_EXECUTE ] }
    },
];
