import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { Authorization } from '@core/model';


@Component({
    selector: 'chutney-scenario-execution-campaign',
    templateUrl: './execution-campaign.component.html',
    styleUrls: ['./execution-campaign.component.scss']
})
export class ScenarioExecutionCampaignComponent implements OnInit, OnDestroy {
    @Input() campaignExecution: object;

    Authorization = Authorization;
    
    ngOnDestroy() {}
    
    ngOnInit() {}

}
