import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ScenarioTextRunComponent } from './scenario-text-run.component';

describe('ScenarioTextRunComponent', () => {
  let component: ScenarioTextRunComponent;
  let fixture: ComponentFixture<ScenarioTextRunComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ScenarioTextRunComponent],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ScenarioTextRunComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
