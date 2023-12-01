/**
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { TestBed, waitForAsync } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { TranslateModule } from '@ngx-translate/core';
import { SharedModule } from '@shared/shared.module';

import { MoleculesModule } from '../../../../molecules/molecules.module';

import { MomentModule } from 'ngx-moment';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';

import { DatasetListComponent } from './dataset-list.component';
import { AngularMultiSelectModule } from 'angular2-multiselect-dropdown';
import { DataSetService } from '@core/services';
import { of } from 'rxjs';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

describe('DatasetListComponent', () => {

  const dataSetService = jasmine.createSpyObj('DataSetService', ['findAll']);
  dataSetService.findAll.and.returnValue(of([]));
   beforeEach(waitForAsync(() => {
    TestBed.resetTestingModule();

    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        RouterTestingModule,
        TranslateModule.forRoot(),
        MoleculesModule,
        SharedModule,
        MomentModule,
        NgbModule,
        AngularMultiSelectModule,
        FormsModule,
        ReactiveFormsModule,
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


