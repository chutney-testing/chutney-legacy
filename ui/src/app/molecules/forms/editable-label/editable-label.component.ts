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
  selector: 'chutney-forms-editable-label',
  templateUrl: './editable-label.component.html',
  styleUrls: ['./editable-label.component.scss']
})
export class EditableLabelComponent {


    @Input() id: string;
    @Input() placeholder: string;
    @Input() type = 'simple';
    @Input() model: string;
    @Input() maxlength = 150;
    @Input() defaultValue = '';
    @Output() modelChange = new EventEmitter<string>();

    constructor() { }

    onInputChange(newValue: string) {
        this.model = newValue;
        this.modelChange.emit(this.model);
    }
}
