import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ScenariosListComponent } from './scenarios-list.component';

describe('ScenariosListComponent', () => {
  let component: ScenariosListComponent;
  let fixture: ComponentFixture<ScenariosListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ScenariosListComponent],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ScenariosListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
