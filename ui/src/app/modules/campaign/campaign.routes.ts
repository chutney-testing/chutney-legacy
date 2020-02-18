import { Routes } from '@angular/router';
import { CampaignListComponent } from './components/campaign-list/campaign-list.component';
import { CampaignExecutionComponent } from './components/execution/execution-campaign.component';
import { CampaignEditionComponent } from './components/create-campaign/campaign-edition.component';

export const CampaignRoute: Routes = [

    { path: '', component: CampaignListComponent },
    { path: ':id/execution', component: CampaignExecutionComponent },
    { path: ':id/edition', component: CampaignEditionComponent },
    { path: 'edition', component: CampaignEditionComponent }
];
