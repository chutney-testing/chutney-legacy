import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SubstepComponent } from './substep.component';

describe('SubstepComponent', () => {
  let component: SubstepComponent;
  let fixture: ComponentFixture<SubstepComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SubstepComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SubstepComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
