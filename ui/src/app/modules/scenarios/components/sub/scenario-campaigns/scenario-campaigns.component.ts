import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { BehaviorSubject, Subscription } from 'rxjs';

import { EventManagerService } from '@shared';
import { CanDeactivatePage } from '@core/guards';
import { CampaignService, ScenarioService } from '@core/services';
import { Campaign } from '@model';

@Component({
    selector: 'chutney-scenario-campaigns',
    templateUrl: './scenario-campaigns.component.html',
    styleUrls: ['./scenario-campaigns.component.scss']
})
export class ScenarioCampaignsComponent implements OnInit, OnDestroy {

    @Input() idScenario: string;
    exampleParams$: BehaviorSubject<any> = new BehaviorSubject<any>({});
    errorMessage: any;
    campaignsForScenario: Array<Campaign> = [];

    private routeParamsSubscription: Subscription;

    constructor(private scenarioService: ScenarioService,
                private campaignService: CampaignService,
                private router: Router,
                private route: ActivatedRoute,
                private eventManager: EventManagerService
    ) {
    }

    ngOnInit() {
        this.routeParamsSubscription = this.route.params.subscribe(() => {
            this.load(this.idScenario);
        });
    }

    ngOnDestroy() {
        this.exampleParams$.unsubscribe();
        this.eventManager.destroy(this.routeParamsSubscription);
    }

    load(id) {
        if (id !== undefined) {
            this.campaignService.findAllCampaignsForScenario(id).subscribe(
                (response) => {
                    this.campaignsForScenario = response;
                },
                (error) => {
                    console.log(error);
                    this.errorMessage = error._body;
                }
            );
        }
    }
}
