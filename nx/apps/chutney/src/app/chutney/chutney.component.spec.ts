import { TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ApolloTestingModule } from 'apollo-angular/testing';

import { UiLayoutModule } from '../../../../libs/ui-layout/src';
import { ChutneyComponent } from './chutney.component';

describe('AppComponent', () => {
  beforeEach(async () => {
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      imports: [
        RouterTestingModule,
        UiLayoutModule,
        BrowserAnimationsModule,
        ApolloTestingModule,
      ],
      declarations: [ChutneyComponent],
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(ChutneyComponent);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it(`should have as title 'chutney'`, () => {
    const fixture = TestBed.createComponent(ChutneyComponent);
    const app = fixture.componentInstance;
    expect(app.title).toEqual('chutney');
  });
});
