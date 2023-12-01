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
import { v4 as uuidv4 } from 'uuid';
import { FormControl } from '@angular/forms';

@Component({
    selector: 'chutney-import-button',
    templateUrl: './import-button.component.html',
    styleUrls: ['./import-button.component.scss']
})
export class ImportButtonComponent implements OnInit {

    @Input() acceptedTypes = '(text/plain)|(application/json)';
    @Output() submitEvent = new EventEmitter();
    @Input() label: string = 'global.actions.import';
    @Input() btnSizeClass: 'lg' | 'sm';
    @Input() btnColor: 'primary' | 'success' | 'info' | 'warning' | 'danger' = 'success';

    uuid: string;

    fileControl = new FormControl('');

    constructor() {
        this.uuid = uuidv4();
    }

    ngOnInit() {

    }

    handleFileSelection(e) {
        e.stopPropagation();
        e.preventDefault();

        const selectedFile = e.target.files[0];
        if (selectedFile) {
            this.import(selectedFile);
        }
        this.fileControl.reset();
    }

    private import(selectedFile: File) {
        this.submitEvent.emit(selectedFile);
    }

}
