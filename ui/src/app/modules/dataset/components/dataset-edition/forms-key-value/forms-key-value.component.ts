import { Component, forwardRef, Input, OnChanges } from '@angular/core';
import {
    AbstractControl,
    ControlValueAccessor,
    FormArray,
    FormBuilder,
    NG_VALIDATORS,
    NG_VALUE_ACCESSOR,
    ValidationErrors
} from '@angular/forms';
import { KeyValue } from '@model';
import { FileSaverService } from 'ngx-filesaver';
import * as JSZip from 'jszip';

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
export class FormsKeyValueComponent implements OnChanges, ControlValueAccessor {

    @Input() keyValues: Array<KeyValue> = [];

    keyValuesForm: FormArray;

    constructor(
        private fb: FormBuilder,
        private fileSaverService: FileSaverService,
    ) {
        this.keyValuesForm = this.fb.array([]);
    }

    // OnChanges
    ngOnChanges(): void {
        this.createForm();
    }

    private createForm() {
        this.clearForm();
        let i = 0;
        this.keyValues.forEach(kv => {
            this.keyValuesForm.insert(
                i,
                this.fb.control(kv)
            );
            i++;
        });
    }

    addKeyValueLine(i?: number) {
        if (i === undefined) {
            i = this.keyValuesForm.length;
        }

        const keyValue = new KeyValue('', '');
        this.keyValuesForm.insert(
            i + 1,
            this.fb.control(keyValue)
        );

        this.keyValues = this.keyValuesForm.getRawValue();
    }

    removeKeyValueLine(i: number) {
        if (i === undefined) {
            i = this.keyValuesForm.length;
        }

        this.keyValuesForm.removeAt(i);
        this.keyValues = this.keyValuesForm.getRawValue();
    }

    updateKey(index: number, event: string) {
        this.keyValuesForm.controls[index].patchValue(
            new KeyValue(event, this.keyValuesForm.value[index].value)
        );
    }

    updateValue(index: number, event: string) {
        this.keyValuesForm.controls[index].patchValue(
            new KeyValue(this.keyValuesForm.value[index].key, event)
        );
    }

    private clearForm() {
        const numberofLine = this.keyValuesForm.controls.length;
        for (let index = 0; index < numberofLine; index++) {
            this.keyValuesForm.removeAt(0);
        }
    }

    // CVA
    onTouched: () => void = () => {
    };

    propagateChange = (_: any) => {
    };

    writeValue(val: any): void {
        val && this.keyValuesForm.setValue(val, { emitEvent: false });
    }

    registerOnChange(fn: any): void {
        this.propagateChange = fn;
        this.keyValuesForm.valueChanges.subscribe(fn);
    }

    registerOnTouched(fn: any): void {
        this.onTouched = fn;
    }

    setDisabledState?(isDisabled: boolean): void {
        isDisabled ? this.keyValuesForm.disable() : this.keyValuesForm.enable();
    }

    validate(c: AbstractControl): ValidationErrors | null {
        return this.keyValuesForm.valid ? null : {
            invalidForm: {
                valid: false,
                message: 'fields are invalid'
            }
        };
    }

    exportKeyValue() {
        const fileName = `keyvalue.csv`;
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
                    this.keyValuesForm.push(
                        this.fb.control(new KeyValue(oneLine[0], oneLine[1]))
                    );
                }
            });
            this.keyValuesForm.enable();
        };
        fileReader.readAsText(file);
    }
}
