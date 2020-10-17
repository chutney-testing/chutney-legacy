import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ScenariosSearchFormComponent } from './scenarios-search-form.component';

describe('ScenariosSearchFormComponent', () => {
  let component: ScenariosSearchFormComponent;
  let fixture: ComponentFixture<ScenariosSearchFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ScenariosSearchFormComponent],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ScenariosSearchFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
