import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CampaignEditComponent } from './campaign-edit.component';

describe('CampaignEditComponent', () => {
  let component: CampaignEditComponent;
  let fixture: ComponentFixture<CampaignEditComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CampaignEditComponent],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CampaignEditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
