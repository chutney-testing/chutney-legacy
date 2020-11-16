import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ExecutionIconComponent } from './execution-icon.component';

describe('ExecutionIconComponent', () => {
  let component: ExecutionIconComponent;
  let fixture: ComponentFixture<ExecutionIconComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ExecutionIconComponent],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ExecutionIconComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
