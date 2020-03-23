import { TestBed, async } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateModule } from '@ngx-translate/core';
import { ScenariiComponent } from './scenarii.component';
import { SharedModule } from '@shared/shared.module';

import { MoleculesModule } from '../../../../molecules/molecules.module';

import { MomentModule } from 'angular2-moment';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { of } from 'rxjs';
import { ScenarioIndex } from '@core/model';
import { ScenarioService } from '@core/services';

describe('ScenariiComponent', () => {

   beforeEach(async(() => {
    TestBed.resetTestingModule();
    const scenarioService = jasmine.createSpyObj('ScenarioService', ['findScenarios']);
    const mockScenarioIndex = [new ScenarioIndex('1', 'title1', 'description', 'source', new Date(), [], []),
                               new ScenarioIndex('2', 'title2', 'description', 'source', new Date(), [], [])];
    scenarioService.findScenarios.and.returnValue(of(mockScenarioIndex));
    TestBed.configureTestingModule({
      imports: [
        RouterTestingModule,
        TranslateModule.forRoot(),
        MoleculesModule,
        SharedModule,
        MomentModule,
        NgbModule
      ],
      declarations: [
        ScenariiComponent
      ],
      providers: [
        { provide: ScenarioService, useValue: scenarioService }
      ]
    }).compileComponents();
  }));

  it('should create the component ScenariiComponent with two scenarios', () => {
    const fixture = TestBed.createComponent(ScenariiComponent);
    fixture.detectChanges();

    const app = fixture.debugElement.componentInstance;
    expect(app).toBeTruthy();

    const html: HTMLElement = fixture.nativeElement;
    const cards = html.querySelectorAll('#cards > chutney-scenario-card');

    expect(cards.length).toBe(2);
    expect(cards[0].querySelector('.scenario-title').textContent).toBe('title1');
    expect(cards[1].querySelector('.scenario-title').textContent).toBe('title2');
    expect(fixture.componentInstance.scenarii.length).toBe(2);
  });
});
