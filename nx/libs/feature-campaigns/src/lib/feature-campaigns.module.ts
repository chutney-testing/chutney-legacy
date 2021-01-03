import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { UiMaterialModule } from '@chutney/ui-material';
import { CampaignsComponent } from './containers/campaigns/campaigns.component';
import { CampaignsListComponent } from './components/campaigns-list/campaigns-list.component';
import { UiCommonsModule } from '@chutney/ui-commons';
import { CampaignViewComponent } from './containers/campaign-view/campaign-view.component';
import { CampaignRunComponent } from './containers/campaign-run/campaign-run.component';
import { UtilsModule } from '@chutney/utils';
import { CampaignEditComponent } from './containers/campaign-edit/campaign-edit.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

@NgModule({
  imports: [
    CommonModule,
    UiCommonsModule,
    UtilsModule,
    RouterModule.forChild([
      { path: '', component: CampaignsComponent },
      { path: 'campaigns', component: CampaignsComponent },
      { path: ':id/view', component: CampaignViewComponent },
      { path: ':id/edit', component: CampaignEditComponent },
      { path: 'add', component: CampaignEditComponent },
      { path: ':id/run/:executionId', component: CampaignRunComponent },
    ]),
    UiMaterialModule,
    ReactiveFormsModule,
    FormsModule,
  ],
  declarations: [
    CampaignsComponent,
    CampaignsListComponent,
    CampaignViewComponent,
    CampaignRunComponent,
    CampaignEditComponent,
  ],
})
export class FeatureCampaignsModule {}
