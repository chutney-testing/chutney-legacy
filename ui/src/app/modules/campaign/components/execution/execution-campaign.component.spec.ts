import { TestBed, async } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { MomentModule } from 'angular2-moment';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { of } from 'rxjs';

import { CampaignExecutionComponent } from './execution-campaign.component';
import { SharedModule } from '@shared/shared.module';
import { MoleculesModule } from '../../../../molecules/molecules.module';
import { ActivatedRouteStub } from 'src/app/testing/activated-route-stub';
import { Campaign, TestCase, CampaignExecutionReport, ScenarioExecutionReportOutline } from '@core/model';
import { ScenarioService, CampaignService, EnvironmentAdminService } from '@core/services';
import { TranslateTestingModule } from '../../../../testing/translate-testing.module';
import { ChartsModule } from 'ng2-charts';


describe('CampaignExecutionComponent', () => {

  const scenarioService = jasmine.createSpyObj('ScenarioService', ['findRawTestCase']);
  const campaignService = jasmine.createSpyObj('CampaignService',
    ['find', 'findAllScenarios', 'executeCampaign', 'delete', 'stopExecution', 'replayFailedScenario']);
  const environmentAdminService = jasmine.createSpyObj('EnvironmentAdminService', ['listEnvironments']);
  const activatedRouteStub = new ActivatedRouteStub();

  campaignService.find.and.returnValue(of(new Campaign()));
  campaignService.findAllScenarios.and.returnValue(of([]));
  campaignService.executeCampaign.and.returnValue(of('no value'));
  campaignService.delete.and.returnValue(of('no value'));
  campaignService.stopExecution.and.returnValue(of('no value'));
  campaignService.replayFailedScenario.and.returnValue(of('no value'));
  scenarioService.findRawTestCase.and.returnValue(of(new TestCase()));
  environmentAdminService.listEnvironments.and.returnValue(of([]));

  beforeEach(() => {
    TestBed.resetTestingModule();
    activatedRouteStub.setParamMap({ id: '1' });
    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        RouterTestingModule,
        TranslateTestingModule,
        MoleculesModule,
        SharedModule,
        MomentModule,
        ChartsModule,
        NgbModule.forRoot()],
      declarations: [
        CampaignExecutionComponent
      ],
      providers: [
        { provide: ActivatedRoute, useValue: activatedRouteStub },
        { provide: ScenarioService, useValue: scenarioService },
        { provide: CampaignService, useValue: campaignService },
        { provide: EnvironmentAdminService, useValue: environmentAdminService },
      ]
    }).compileComponents();
  });

  it('should create the component CampaignExecutionComponent', (() => {
    const fixture = TestBed.createComponent(CampaignExecutionComponent);
    fixture.detectChanges();
    const app = fixture.debugElement.componentInstance;
    expect(app).toBeTruthy();
  }));

  it('should aggregate campaign report status to summarize them', (() => {
    const campaignMock = new Campaign();
    const campaignReport = new CampaignExecutionReport();
    campaignMock.campaignExecutionReports.push(campaignReport);

    const reportOK = new ScenarioExecutionReportOutline();
    reportOK.status = 'SUCCESS';
    const reportKO = new ScenarioExecutionReportOutline();
    reportKO.status = 'FAILURE';
    const reportSTOPPED = new ScenarioExecutionReportOutline();
    reportSTOPPED.status = 'STOPPED';

    campaignReport.scenarioExecutionReports.push(reportOK);
    campaignReport.scenarioExecutionReports.push(reportOK);
    campaignReport.scenarioExecutionReports.push(reportOK);
    campaignReport.scenarioExecutionReports.push(reportKO);
    campaignReport.scenarioExecutionReports.push(reportKO);
    campaignService.find.and.returnValue(of(campaignMock));

    const fixture = TestBed.createComponent(CampaignExecutionComponent);
    const html: HTMLElement = fixture.nativeElement;
    fixture.detectChanges();

    const c = fixture.componentInstance;
    expect(c.last.passed).toBe(3);
    expect(c.last.failed).toBe(2);
    expect(c.last.stopped).toBe(0);
    expect(c.last.notexecuted).toBe(0);

    const scenarioSummary = html.querySelector('#scenarioSummary');
    expect(scenarioSummary.textContent).toBe(' 3 OK  2 KO ');
  }));
});


