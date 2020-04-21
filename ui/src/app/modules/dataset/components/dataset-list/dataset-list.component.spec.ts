import { TestBed, async } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateModule } from '@ngx-translate/core';
import { SharedModule } from '@shared/shared.module';

import { MoleculesModule } from '../../../../molecules/molecules.module';

import { MomentModule } from 'angular2-moment';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';

import { DatasetListComponent } from './dataset-list.component';
import { AngularMultiSelectModule } from 'angular2-multiselect-dropdown';
import { DataSetService } from '@core/services';

describe('DatasetListComponent', () => {

  const dataSetService = jasmine.createSpyObj('DataSetService', ['findAll']);

   beforeEach(async(() => {
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      imports: [
        RouterTestingModule,
        TranslateModule.forRoot(),
        MoleculesModule,
        SharedModule,
        MomentModule,
        NgbModule,
        AngularMultiSelectModule
      ],
      declarations: [
        DatasetListComponent
      ],
      providers: [
        { provide: DataSetService, useValue: dataSetService }
      ]
    }).compileComponents();
  }));

  it('should create the component DatasetListComponent', () => {
    const fixture = TestBed.createComponent(DatasetListComponent);
    fixture.detectChanges();

    const app = fixture.debugElement.componentInstance;
    expect(app).toBeTruthy();
  });

});


