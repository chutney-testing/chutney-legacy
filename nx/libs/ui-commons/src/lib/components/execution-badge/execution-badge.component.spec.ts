import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ExecutionBadgeComponent } from './execution-badge.component';

describe('ExecutionBadgeComponent', () => {
  let component: ExecutionBadgeComponent;
  let fixture: ComponentFixture<ExecutionBadgeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ExecutionBadgeComponent],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ExecutionBadgeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
