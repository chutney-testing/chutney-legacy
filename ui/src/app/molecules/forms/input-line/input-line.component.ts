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

import { Component, Input, Output, EventEmitter } from '@angular/core';

@Component({
selector: 'chutney-forms-input-line',
templateUrl: './input-line.component.html',
styleUrls: ['./input-line.component.scss']
})
export class InputLineComponent {

    @Input() id: string;
    @Input() label: string;
    @Input() placeholder: string;
    @Input() type = 'text';
    @Input() model: string;
    @Output() modelChange = new EventEmitter<string>();
    @Input() validate: (value: string) => boolean = (_) => true;

    constructor() { }

    onInputChange(newValue: string) {
        this.model = newValue;
        this.modelChange.emit(this.model);
    }
}
