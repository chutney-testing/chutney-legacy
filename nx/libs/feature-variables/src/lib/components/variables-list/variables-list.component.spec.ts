import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VariablesListComponent } from './variables-list.component';

describe('VariablesListComponent', () => {
  let component: VariablesListComponent;
  let fixture: ComponentFixture<VariablesListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ VariablesListComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(VariablesListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
