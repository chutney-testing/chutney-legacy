import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { Authorization, CampaignExecutionReport } from '@core/model';


@Component({
    selector: 'chutney-scenario-execution-campaign',
    templateUrl: './execution-campaign.component.html',
    styleUrls: ['./execution-campaign.component.scss']
})
export class ScenarioExecutionCampaignComponent implements OnInit, OnDestroy {
    @Input() campaignExecutionReport: CampaignExecutionReport;

    Authorization = Authorization;
    
    ngOnDestroy() {}
    
    ngOnInit() {}

}
