import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ScenarioTextEditComponent } from './scenario-text-edit.component';

describe('ScenarioTextExitComponent', () => {
  let component: ScenarioTextEditComponent;
  let fixture: ComponentFixture<ScenarioTextEditComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ScenarioTextEditComponent],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ScenarioTextEditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
