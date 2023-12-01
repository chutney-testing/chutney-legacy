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
import { TranslateModule } from '@ngx-translate/core';
import { SharedModule } from '@shared/shared.module';

import { MoleculesModule } from '../../../../molecules/molecules.module';

import { MomentModule } from 'ngx-moment';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';

import { DatasetEditionComponent } from './dataset-edition.component';
import { AngularMultiSelectModule } from 'angular2-multiselect-dropdown';
import { DataSetService } from '@core/services';
import { of } from 'rxjs';
import { DatasetListComponent } from '@modules/dataset/components/dataset-list/dataset-list.component';
import { FormsKeyValueComponent } from '@modules/dataset/components/dataset-edition/forms-key-value/forms-key-value.component';
import { FormsDataGridComponent } from '@modules/dataset/components/dataset-edition/forms-data-grid/forms-data-grid.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { FeatureService } from '@core/feature/feature.service';

describe('DatasetEditionComponent', () => {

    const dataSetService = jasmine.createSpyObj('DataSetService', ['findAll']);
    dataSetService.findAll.and.returnValue(of([]));

    const featureService = jasmine.createSpyObj('FeatureService', ['active']);
    featureService.active.and.returnValue(false);

    beforeEach(waitForAsync(() => {
        TestBed.resetTestingModule();
        TestBed.configureTestingModule({
            imports: [
                RouterTestingModule,
                TranslateModule.forRoot(),
                MoleculesModule,
                SharedModule,
                MomentModule,
                NgbModule,
                AngularMultiSelectModule,
                FormsModule,
                ReactiveFormsModule
            ],
            declarations: [
                DatasetListComponent,
                DatasetEditionComponent,
                FormsKeyValueComponent,
                FormsDataGridComponent
            ],
            providers: [
                {provide: DataSetService, useValue: dataSetService},
                {provide: FeatureService, useValue: featureService}
            ]
        }).compileComponents();
    }));

    it('should create the component DatasetEditionComponent', () => {
        const fixture = TestBed.createComponent(DatasetEditionComponent);
        fixture.detectChanges();

        const app = fixture.debugElement.componentInstance;
        expect(app).toBeTruthy();
    });

});


