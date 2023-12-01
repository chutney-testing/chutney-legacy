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

import { Component, Input, Output, EventEmitter, OnChanges } from '@angular/core';

@Component({
    selector: 'chutney-environment-combo',
    templateUrl: './environment-combo.component.html',
    styleUrls: ['./environment-combo.component.scss']
})
export class EnvironmentComboComponent implements OnChanges {

    @Input() environments: Array<string>;
    @Input() defaultValue: string;
    @Output() selectionEvent = new EventEmitter();

    selectedEnvironment: string;

    constructor() {
    }

    ngOnChanges() {
        this.setSelectedEnvironment();
    }

    changingValue(event: any) {
        this.selectedEnvironment = this.environments.filter(env => env === event.target.value)[0];
        this.selectionEvent.emit(this.selectedEnvironment);
    }

    private setSelectedEnvironment() {
        if (this.environments && this.environments.length > 0) {
            const envFound = this.environments.find(e => e === this.defaultValue);
            if (envFound) {
                this.selectedEnvironment = envFound;
            } else {
                this.selectedEnvironment = this.environments[0];
            }
            this.selectionEvent.emit(this.selectedEnvironment);
        }
    }
}
