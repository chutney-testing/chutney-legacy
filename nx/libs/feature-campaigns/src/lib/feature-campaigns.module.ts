import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { UiMaterialModule } from '@chutney/ui-material';
import { CampaignsComponent } from './containers/campaigns/campaigns.component';
import { CampaignsListComponent } from './components/campaigns-list/campaigns-list.component';
import { UiCommonsModule } from '@chutney/ui-commons';

@NgModule({
  imports: [
    CommonModule,
    UiCommonsModule,
    RouterModule.forChild([
      { path: '', component: CampaignsComponent },
      { path: 'campaigns', component: CampaignsComponent },
    ]),
    UiMaterialModule,
  ],
  declarations: [CampaignsComponent, CampaignsListComponent],
})
export class FeatureCampaignsModule {}
