import { TestBed, async, fakeAsync, tick } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateModule } from '@ngx-translate/core';
import { SharedModule } from '@shared/shared.module';

import { MoleculesModule } from '../../../../../molecules/molecules.module';

import { MomentModule } from 'angular2-moment';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { of } from 'rxjs';
import { ComponentService } from '@core/services';
import { ComponentEditionComponent } from './component-edition.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ReactiveFormsModule, FormsModule, FormArray } from '@angular/forms';
import { DragulaService, DragulaModule } from 'ng2-dragula';
import { ScenarioCampaignsComponent } from '../../sub/scenario-campaigns/scenario-campaigns.component';
import { ActivatedRouteStub } from 'src/app/testing/activated-route-stub';
import { ActivatedRoute } from '@angular/router';
import { ComponentTask, ScenarioComponent, KeyValue } from '@core/model';

// ng2-dragula is hard to test.  @angular-skyhook should be a good replacement
describe('ComponentEditionComponent', () => {

  const componentService = jasmine.createSpyObj('ComponentService', ['findAllComponent', 'saveComponentTestCase', 'findComponentTestCase']);
  const dragulaService = new DragulaService();
  dragulaService.createGroup('FAKE', {});
  const activatedRouteStub = new ActivatedRouteStub();

  const task1 = new ComponentTask('name 1', null, [], [new KeyValue('1-param', '')], [new KeyValue('1-param', 'value1')],
                                                                                                    [], null, 'id-1');
  const task2 = new ComponentTask('name 2', null, [], [], [], [], null, 'id-2');

  beforeEach(async(() => {
    TestBed.resetTestingModule();
    activatedRouteStub.setParamMap({ id: '42' });
    activatedRouteStub.setSnapshotQueryParamMap({ duplicate: false });

    componentService.findAllComponent.and.returnValue(of([task1, task2]));

    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        RouterTestingModule,
        TranslateModule.forRoot(),
        MoleculesModule,
        SharedModule,
        FormsModule,
        ReactiveFormsModule,
        MomentModule,
        DragulaModule,
        NgbModule,
      ],
      declarations: [
        ScenarioCampaignsComponent,
        ComponentEditionComponent,
      ],
      providers: [
        { provide: ActivatedRoute, useValue: activatedRouteStub },
        { provide: ComponentService, useValue: componentService },
        { provide: DragulaService, useValue: dragulaService }
      ]
    }).compileComponents();
  }));

  it('should create the component ComponentEditionComponent', () => {
    componentService.findComponentTestCase.and.returnValue(of(new ScenarioComponent()));

    const fixture = TestBed.createComponent(ComponentEditionComponent);
    fixture.detectChanges();

    const c = fixture.componentInstance;
    expect(c.canDeactivatePage()).toBe(true);

    const app = fixture.debugElement.componentInstance;
    expect(app).toBeTruthy();
  });

  it('Update tags should trigger scenario modified', () => {
    const componentTestCaseMock = new ScenarioComponent('id', 'my title', 'my description', new Date(), [], [], [], ['tag1', 'tag2']);
    componentService.findComponentTestCase.and.returnValue(of(componentTestCaseMock));
    const fixture = TestBed.createComponent(ComponentEditionComponent);
    fixture.detectChanges();

    const html: HTMLElement = fixture.nativeElement;
    const scenarioSummary: HTMLTextAreaElement = html.querySelector('#tags');
    expect(scenarioSummary.value).toBe('tag1,tag2');

    const c = fixture.componentInstance;
    c.componentForm.controls['tags'].setValue('tag1,tag2,anothertagg');

    expect(c.canDeactivatePage()).toBe(false);
  });

  it('Update sub steps should trigger scenario modified', () => {

    const componentTestCaseMock = new ScenarioComponent('id', '', '', new Date(), [task1, task2], [], [], []);
    componentService.findComponentTestCase.and.returnValue(of(componentTestCaseMock));
    const fixture = TestBed.createComponent(ComponentEditionComponent);
    fixture.detectChanges();

    const c = fixture.componentInstance;
    c.componentTasksCreated.push(task1);
    c.componentTasksCreated.push(task2);

    expect(c.canDeactivatePage()).toBe(false);
  });

  it('Update scenario parameter should trigger scenario modified', () => {
    const componentTestCaseMock = new ScenarioComponent('id', '', '', new Date(), [], [new KeyValue('key1', 'value1')], [], []);
    componentService.findComponentTestCase.and.returnValue(of(componentTestCaseMock));
    const fixture = TestBed.createComponent(ComponentEditionComponent);
    fixture.detectChanges();

    const html: HTMLElement = fixture.nativeElement;
    const addParameterButton: HTMLButtonElement = html.querySelector('#addParameterBtn');
    addParameterButton.click();
    fixture.detectChanges();

    const c = fixture.componentInstance;
    const parametersArray = c.componentForm.controls['parameters'] as FormArray;
    expect(parametersArray.length).toBe(2);

    expect(c.canDeactivatePage()).toBe(false);
  });

  it('Update component dataset  should trigger scenario modified', () => {
    const componentTestCaseMock = new ScenarioComponent('id', '', '', new Date(), [task1, task2],
                                                                [new KeyValue('param', 'value1')], [new KeyValue('dataset', 'value1')], []);
    componentService.findComponentTestCase.and.returnValue(of(componentTestCaseMock));
    const fixture = TestBed.createComponent(ComponentEditionComponent);
    fixture.detectChanges();

    const c = fixture.componentInstance;
    const componentsValues = c.componentForm.controls.componentsValues as FormArray;
    componentsValues.get('0').get('0').setValue('anotherValuefordataset');

    expect(c.canDeactivatePage()).toBe(false);
  });
});
