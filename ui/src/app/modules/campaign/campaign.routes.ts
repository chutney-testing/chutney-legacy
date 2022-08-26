import { Routes } from '@angular/router';

import { CampaignListComponent } from './components/campaign-list/campaign-list.component';
import { CampaignExecutionComponent } from './components/execution/execution-campaign.component';
import { CampaignEditionComponent } from './components/create-campaign/campaign-edition.component';
import { CampaignSchedulingComponent } from './components/campaign-scheduling/campaign-scheduling.component';
import { AuthGuard } from '@core/guards';
import { Authorization } from '@model';
import { ChutneyMainHeaderComponent } from '@shared/components/chutney-main-header/chutney-main-header.component';
import { ChutneyLeftMenuComponent } from '@shared/components/chutney-left-menu/chutney-left-menu.component';
import { ChutneyRightMenuComponent } from '@shared/components/chutney-right-menu/chutney-right-menu.component';

export const CampaignRoute: Routes = [
    {
        path: '',
        component: CampaignListComponent,
        canActivate: [AuthGuard],
        data: { 'authorizations': [ Authorization.CAMPAIGN_READ ] }
    },
    {
        path: ':id/execution',
        redirectTo: ':id/execution/last',
        pathMatch: 'full'
    },
    {
        path: ':id/execution/:execId',
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
