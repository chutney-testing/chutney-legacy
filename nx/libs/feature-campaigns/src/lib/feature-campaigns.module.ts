import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { UiMaterialModule } from '@chutney/ui-material';
import { CampaignsComponent } from './containers/campaigns/campaigns.component';
import { CampaignsListComponent } from './components/campaigns-list/campaigns-list.component';
import { UiCommonsModule } from '@chutney/ui-commons';
import { CampaignViewComponent } from './containers/campaign-view/campaign-view.component';
import { CampaignRunComponent } from './containers/campaign-run/campaign-run.component';

@NgModule({
  imports: [
    CommonModule,
    UiCommonsModule,
    RouterModule.forChild([
      { path: '', component: CampaignsComponent },
      { path: 'campaigns', component: CampaignsComponent },
      { path: ':id/view', component: CampaignViewComponent },
    ]),
    UiMaterialModule,
  ],
  declarations: [
    CampaignsComponent,
    CampaignsListComponent,
    CampaignViewComponent,
    CampaignRunComponent,
  ],
})
export class FeatureCampaignsModule {}
