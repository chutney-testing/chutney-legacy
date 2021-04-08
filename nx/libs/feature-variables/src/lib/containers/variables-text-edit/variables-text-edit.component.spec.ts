import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VariablesTextEditComponent } from './variables-text-edit.component';

describe('VariablesTextEditComponent', () => {
  let component: VariablesTextEditComponent;
  let fixture: ComponentFixture<VariablesTextEditComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ VariablesTextEditComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(VariablesTextEditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
