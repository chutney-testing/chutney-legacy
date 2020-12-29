import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { chutneyAnimations } from '@chutney/utils';
import { Observable } from 'rxjs';
import { NestedTreeControl } from '@angular/cdk/tree';
import { MatTreeNestedDataSource } from '@angular/material/tree';
import { map, pluck, switchMap, tap } from 'rxjs/operators';
import { ActivatedRoute, Router } from '@angular/router';
import { MediaObserver } from '@angular/flex-layout';
import {
  CampaignExecutionReport,
  CampaignExecutionReportGQL,
  RunCampaignGQL,
  StopCampaignGQL,
} from '@chutney/data-access';

const formSerializer = () => {
  return {};
};

@Component({
  selector: 'chutney-campaign-run',
  templateUrl: './campaign-run.component.html',
  styleUrls: ['./campaign-run.component.scss'],
  animations: [chutneyAnimations],
})
export class CampaignRunComponent implements OnInit {
  private campaignId: string;
  private executionId: string;
  queryRef: any;
  treeControl = new NestedTreeControl<any>((node) => node.steps);
  dataSource = new MatTreeNestedDataSource<any>();
  report: CampaignExecutionReport;
  breadcrumbs: any = [
    { title: 'Home', link: ['/'] },
    { title: 'Campaigns', link: ['/'] },
    { title: 'View', link: ['../../view'] },
  ];
  environments: any = ['GLOBAL', 'PERF'];
  hasChild = (_: number, node: any) =>
    !!node.subSteps && node.subSteps.length > 0;

  isHandset$: Observable<boolean> = this.mediaObserver.asObservable().pipe(
    map(
      () =>
        this.mediaObserver.isActive('xs') ||
        this.mediaObserver.isActive('sm') ||
        this.mediaObserver.isActive('lt-md')
    ),
    tap(() => this.changeDetectorRef.detectChanges())
  );
  running: boolean;
  environment: any;
  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private mediaObserver: MediaObserver,
    private changeDetectorRef: ChangeDetectorRef,
    private runCampaignGQL: RunCampaignGQL,
    private stopCampaignGQL: StopCampaignGQL,
    private campaignExecutionReportGQL: CampaignExecutionReportGQL
  ) {}

  ngOnInit(): void {
    this.route.params
      .pipe(
        switchMap((p) => {
          this.campaignId = p.id;
          this.executionId = p.executionId;
          this.queryRef = this.campaignExecutionReportGQL.watch(
            { campaignId: p.id, executionId: p.executionId },
            { pollInterval: 1000 }
          );
          return this.queryRef.valueChanges;
        })
      )
      .pipe(pluck('data', 'campaignExecutionReport'))
      .subscribe((data: CampaignExecutionReport) => {
        this.report = data;
        this.running = this.report.status === 'RUNNING';
        this.environment = this.report.executionEnvironment;
        this.dataSource.data = this.report.scenarioExecutionReports;
        if (this.report.status != 'RUNNING') {
          this.queryRef.stopPolling();
        }
      });
  }

  stopCampaign() {
    this.stopCampaignGQL
      .mutate({
        campaignId: this.campaignId,
        executionId: this.executionId,
        bodyBuilder: formSerializer,
      })
      .subscribe((result) => {
        //this.report = {...this.report, status: "STOPPED"}
        this.running = false;
      });
  }

  runCampaign() {
    this.runCampaignGQL
      .mutate({
        campaignId: this.campaignId,
        environment: this.environment,
      })
      .subscribe((result) =>
        this.router.navigate([`../${result.data.runCampaign.executionId}`], {
          relativeTo: this.route,
        })
      );
  }

  select(environment: any) {
    this.environment = environment;
  }
}
