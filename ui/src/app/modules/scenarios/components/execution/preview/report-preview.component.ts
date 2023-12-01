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

import { Component } from '@angular/core';
import { Execution } from '@core/model';

@Component({
    selector: 'chutney-report-preview',
    templateUrl: './report-preview.component.html',
    styleUrls: ['./report-preview.component.scss']
})
export class ReportPreviewComponent {

    scenarioName: string;
    execution: Execution;
    errorMessage: string;

    preview(file: File) {
        this.execution = null;
        this.scenarioName = '';
        this.errorMessage = null;
        file.text()
            .then(data => {
                this.execution = Execution.deserialize(JSON.parse(data));
                this.scenarioName = JSON.parse(this.execution.report).scenarioName;
            })
            .catch(error => {
                this.errorMessage = error;
            });
    }
}
