import { async, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateModule } from '@ngx-translate/core';
import { ScenariosComponent } from './scenarios.component';
import { SharedModule } from '@shared/shared.module';

import { MoleculesModule } from '../../../../molecules/molecules.module';

import { MomentModule } from 'angular2-moment';
import { NgbModule, NgbPopoverConfig } from '@ng-bootstrap/ng-bootstrap';
import { of } from 'rxjs';
import { ScenarioIndex } from '@core/model';
import { ScenarioService } from '@core/services';
import { ActivatedRoute } from '@angular/router';
import { ActivatedRouteStub } from '../../../../testing/activated-route-stub';

describe('ScenariosComponent', () => {
    let activatedRouteStub;

    beforeEach(async(() => {
        TestBed.resetTestingModule();
        const scenarioService = jasmine.createSpyObj('ScenarioService', ['findScenarios']);
        const mockScenarioIndex = [new ScenarioIndex('1', 'title1', 'description', 'source', new Date(), [], []),
                                   new ScenarioIndex('2', 'title2', 'description', 'source', new Date(), [], []),
                                   new ScenarioIndex('3', 'another scenario', 'description', 'source', new Date(), [], [])];
        scenarioService.findScenarios.and.returnValue(of(mockScenarioIndex));
        activatedRouteStub = new ActivatedRouteStub();
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
                ScenariosComponent
            ],
            providers: [
                NgbPopoverConfig,
                {provide: ScenarioService, useValue: scenarioService},
                {provide: ActivatedRoute, useValue: activatedRouteStub}
            ]
        }).compileComponents();
    }));

    it('should create the component ScenariosComponent with three scenarios', async(() => {
        const fixture = TestBed.createComponent(ScenariosComponent);
        fixture.detectChanges();
        fixture.whenStable().then(() => {
            fixture.detectChanges();
            const app = fixture.debugElement.componentInstance;
            expect(app).toBeTruthy();
            const html: HTMLElement = fixture.nativeElement;
            const cards = getCards(html);
            expect(cards.length).toBe(3);
            expect(titleOf(cards[0])).toBe('title1');
            expect(titleOf(cards[1])).toBe('title2');
            expect(titleOf(cards[2])).toBe('another scenario');
            expect(fixture.componentInstance.scenarios.length).toBe(3);
        });
    }));

    it('should filter the list of scenario', async(() => {
        const fixture = TestBed.createComponent(ScenariosComponent);
        fixture.detectChanges();
        fixture.whenStable().then(() => {
            const html: HTMLElement = fixture.nativeElement;

            const searchInput: HTMLInputElement = html.querySelector('#scenario-search');
            sendInput(searchInput, 'another');
            fixture.detectChanges();

            const cards = getCards(html);
            expect(cards.length).toBe(1);
            expect(titleOf(cards[0])).toBe('another scenario');
        });
    }));

    it('should apply filters from the URL', async(() => {
        const fixture = TestBed.createComponent(ScenariosComponent);
        activatedRouteStub.setParamMap({ text: 'title', orderBy: 'title', reverseOrder: true});
        fixture.detectChanges();
        fixture.whenStable().then(() => {
            fixture.detectChanges();
            const app = fixture.debugElement.componentInstance;
            expect(app).toBeTruthy();
            const html: HTMLElement = fixture.nativeElement;
            const cards = getCards(html);

            expect(cards.length).toBe(2);
            expect(titleOf(cards[0])).toBe('title2');
            expect(titleOf(cards[1])).toBe('title1');
            expect(fixture.componentInstance.scenarios.length).toBe(3);
        });
    }));

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
