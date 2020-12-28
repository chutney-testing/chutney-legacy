import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { Campaign, CampaignGQL, RunCampaignGQL } from '@chutney/data-access';
import { ActivatedRoute, Router } from '@angular/router';
import { chutneyAnimations } from '@chutney/utils';
import { map, pluck } from 'rxjs/operators';
import { NestedTreeControl } from '@angular/cdk/tree';
import { MatTreeNestedDataSource } from '@angular/material/tree';

@Component({
  selector: 'chutney-campaign-view',
  templateUrl: './campaign-view.component.html',
  styleUrls: ['./campaign-view.component.scss'],
  animations: [chutneyAnimations],
})
export class CampaignViewComponent implements OnInit {
  campaignId: string;
  campaign$: Observable<Campaign>;
  breadcrumbs: any = [
    { title: 'Home', link: ['/'] },
    { title: 'Campaigns', link: ['/'] },
  ];
  environments: string[] = ['GLOBAL', 'PERF'];
  treeControl = new NestedTreeControl<any>((node) => node.subSteps);
  dataSource = new MatTreeNestedDataSource<any>();
  hasChild = (_: number, node: any) =>
    !!node.subSteps && node.subSteps.length > 0;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private campaignGQL: CampaignGQL,
    private runCampaignGQL: RunCampaignGQL
  ) {}

  ngOnInit(): void {
    this.campaignId = this.route.snapshot.paramMap.get('id');
    this.campaign$ = this.campaignGQL
      .watch({ campaignId: this.campaignId })
      .valueChanges.pipe(pluck('data', 'campaign'));
    this.campaign$.subscribe(
      (campaign: Campaign) => (this.dataSource.data = campaign.scenarios)
    );
  }

  editCampaign(campaignId: string) {
    this.router.navigate([`../edit`], {
      relativeTo: this.route,
    });
  }

  runCampaign(campaignId: string, environment: string) {
    this.runCampaignGQL
      .mutate({ campaignId: campaignId, environment: environment, dataset: [] })
      .subscribe((result) =>
        this.router.navigate(
          [`../run/${result.data.runCampaign.executionId}`],
          {
            relativeTo: this.route,
          }
        )
      );
  }
}
