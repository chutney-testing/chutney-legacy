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

import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { DataSetService } from '@core/services';
import { Dataset } from '@model';
import { FeatureService } from '@core/feature/feature.service';
import { FeatureName } from '@core/feature/feature.model';


@Component({
    selector: 'chutney-dataset-selection',
    templateUrl: './dataset-selection.component.html',
    styleUrls: ['./dataset-selection.component.scss']
})
export class DatasetSelectionComponent implements OnInit {

    @Input() selectedDatasetId: String;
    @Output() selectionEvent = new EventEmitter();

    datasets: Array<Dataset>;

    constructor(private datasetService: DataSetService) {}

    ngOnInit(): void {
        this.datasetService.findAll().subscribe((res: Array<Dataset>) => {
            this.datasets = res;
        });
    }

    changingValue(event: any) {
        this.selectionEvent.emit(event.target.value);
    }

}
