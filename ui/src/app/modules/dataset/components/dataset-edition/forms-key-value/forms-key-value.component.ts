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

import { Component, forwardRef } from '@angular/core';
import {
    AbstractControl,
    ControlValueAccessor,
    FormArray,
    FormBuilder, FormGroup,
    NG_VALIDATORS,
    NG_VALUE_ACCESSOR,
    ValidationErrors
} from '@angular/forms';
import { KeyValue } from '@model';
import { FileSaverService } from 'ngx-filesaver';

@Component({
    selector: 'chutney-forms-key-value',
    templateUrl: './forms-key-value.component.html',
    styleUrls: ['./forms-key-value.component.scss'],
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => FormsKeyValueComponent),
            multi: true
        },
        {
            provide: NG_VALIDATORS,
            useExisting: forwardRef(() => FormsKeyValueComponent),
            multi: true
        }
    ]
})
export class FormsKeyValueComponent implements ControlValueAccessor {

    keyValuesForm: FormArray;
    isDisabled: boolean;

    constructor(
        private fb: FormBuilder,
        private fileSaverService: FileSaverService,
    ) {
        this.keyValuesForm = this.fb.array([]);
    }

    insertNewKeyValue(i?: number) {
        this.addKeyValue('', '', i+1);
    }

    private addKeyValue(key?: string, value?: string, i?: number): void {
        if (i == null) {
            i = this.keyValuesForm.length + 1;
        }

        this.keyValuesForm.insert(i, this.createKeyValue(key, value));
    }

    private createKeyValue(key?: string, value?: string): FormGroup {
        return this.fb.group({
            key: key ? key : '',
            value: value ? value : ''
        });
    }

    removeKeyValueLine(i?: number) {
        if (i == null) {
            i = this.keyValuesForm.length;
        }

        this.keyValuesForm.removeAt(i);
    }

    private clearForm() {
        const lineCount = this.keyValuesForm.controls.length;
        for (let index = 0; index < lineCount; index++) {
            this.keyValuesForm.removeAt(0);
        }
    }

    exportKeyValue() {
        const fileName = 'chutney_dataset_keyvalues.csv';
        let fileContent = '';
        const delimiter = ';';

        this.keyValuesForm.value.forEach(element => {
            fileContent += element.key + delimiter + element.value + '\n';
        });

        this.fileSaverService.saveText(fileContent, fileName);
    }

    importKeyValue(files: FileList) {
        const file = files.item(0);
        const fileReader = new FileReader();
        fileReader.onload = (e) => {
            this.clearForm();
            const content = '' + fileReader.result;
            const lines = content.split('\n');
            lines.forEach(l => {
                const oneLine = l.split(';');
                if (oneLine.length === 2) {
                    this.addKeyValue(oneLine[0], oneLine[1]);
                }
            });
            this.keyValuesForm.enable();
        };
        fileReader.readAsText(file);
    }

    // CVA
    onChanged: any = () => {};

    onTouched: any = () => {};

    propagateChange: any = () => {};

    writeValue(val: Array<KeyValue>): void {
        this.clearForm();
        if (val != null && val.length > 0) {
            if (this.keyValuesForm.length === 0) {
                val.forEach(kv => {
                   this.addKeyValue(kv.key, kv.value)
                });
            }
        }
    }

    registerOnChange(fn: any): void {
        this.propagateChange = fn;
        this.keyValuesForm.valueChanges.subscribe(fn);
    }

    registerOnTouched(fn: any): void {
        this.onTouched = fn;
    }

    setDisabledState(isDisabled: boolean): void {
        this.isDisabled = isDisabled;
    }

    validate(c: AbstractControl): ValidationErrors | null {
        return this.keyValuesForm.valid ? null : {
            invalidForm: {
                valid: false,
                message: 'fields are invalid'
            }
        };
    }
}
