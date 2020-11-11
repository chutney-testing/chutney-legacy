import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ScenarioTextViewComponent } from './scenario-text-view.component';

describe('ScenarioTextViewComponent', () => {
  let component: ScenarioTextViewComponent;
  let fixture: ComponentFixture<ScenarioTextViewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ScenarioTextViewComponent],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ScenarioTextViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
