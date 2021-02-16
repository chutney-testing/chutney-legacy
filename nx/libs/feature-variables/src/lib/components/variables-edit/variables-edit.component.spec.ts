import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VariablesEditComponent } from './variables-edit.component';

describe('VariablesEditComponent', () => {
  let component: VariablesEditComponent;
  let fixture: ComponentFixture<VariablesEditComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ VariablesEditComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(VariablesEditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
