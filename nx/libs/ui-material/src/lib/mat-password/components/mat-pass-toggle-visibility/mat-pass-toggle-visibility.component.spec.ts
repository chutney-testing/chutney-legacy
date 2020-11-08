import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MatPassToggleVisibilityComponent } from './mat-pass-toggle-visibility.component';

describe('MatPassToggleVisibilityComponent', () => {
  let component: MatPassToggleVisibilityComponent;
  let fixture: ComponentFixture<MatPassToggleVisibilityComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [MatPassToggleVisibilityComponent],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MatPassToggleVisibilityComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
