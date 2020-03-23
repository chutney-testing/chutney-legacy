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
                               new ScenarioIndex('2', 'title2', 'description', 'source', new Date(), [], []),
                               new ScenarioIndex('3', 'another scenario', 'description', 'source', new Date(), [], [])];
    scenarioService.findScenarios.and.returnValue(of(mockScenarioIndex));
    TestBed.configureTestingModule({
      imports: [
        RouterTestingModule,
        TranslateModule.forRoot(),
        MoleculesModule,
        SharedModule,
        MomentModule,
        NgbModule,
      ],
      declarations: [
        ScenariiComponent
      ],
      providers: [
        { provide: ScenarioService, useValue: scenarioService }
      ]
    }).compileComponents();
  }));

  it('should create the component ScenariiComponent with three scenarios', () => {
    const fixture = TestBed.createComponent(ScenariiComponent);
    fixture.detectChanges();

    const app = fixture.debugElement.componentInstance;
    expect(app).toBeTruthy();

    const html: HTMLElement = fixture.nativeElement;
    const cards = getCards(html);

    expect(cards.length).toBe(3);
    expect(titleOf(cards[0])).toBe('another scenario');
    expect(titleOf(cards[1])).toBe('title1');
    expect(titleOf(cards[2])).toBe('title2');
    expect(fixture.componentInstance.scenarii.length).toBe(3);
  });

  it('should filter the list of scenario', () => {
    const fixture = TestBed.createComponent(ScenariiComponent);
    fixture.detectChanges();

    const html: HTMLElement = fixture.nativeElement;

    const searchInput: HTMLInputElement = html.querySelector('#scenario-search');
    sendInput(searchInput, 'another');
    fixture.detectChanges();

    const cards = getCards(html);
    expect(cards.length).toBe(1);
    expect(titleOf(cards[0])).toBe('another scenario');
  });
});

function getCards(html: HTMLElement) {
  return html.querySelectorAll('#cards > chutney-scenario-card');
}

function titleOf(elt: Element) {
  return elt.querySelector('.scenario-title').textContent;
}

function sendInput(input: HTMLInputElement, value: string) {
  input.value = value;
  input.dispatchEvent(new Event('input'));
}
