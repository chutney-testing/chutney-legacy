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

import { Component, EventEmitter, Input, Output } from '@angular/core';

import { Entry } from '@model';
import { ValidationService } from '../../validation/validation.service';

@Component({
    selector: 'chutney-property-table-panel',
    templateUrl: './property-table-panel.component.html',
    styleUrls: ['./property-table-panel.component.scss']
})
export class PropertyTablePanelComponent {

    @Input() entries: Entry[];
    @Output() entriesChange: EventEmitter<Entry[]> = new EventEmitter<Entry[]>();


    constructor(public validationService: ValidationService) { }

    createEntry() {
        if (!this.entries) {
            this.entries = [];
        }
        this.entries.unshift(new Entry('', ''));
        this.entriesChange.emit(this.entries)
    }

    deleteEntry(entry: Entry) {
        this.entries.splice(this.entries.indexOf(entry), 1);
        this.entriesChange.emit(this.entries)
    }
}
