import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CampaignRunComponent } from './campaign-run.component';

describe('CampaignRunComponent', () => {
  let component: CampaignRunComponent;
  let fixture: ComponentFixture<CampaignRunComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CampaignRunComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CampaignRunComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
