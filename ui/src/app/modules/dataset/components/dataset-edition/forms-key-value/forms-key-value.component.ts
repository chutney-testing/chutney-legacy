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
    registeredControls: number[] = [];

    constructor(private fb: FormBuilder) {
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
            this.registeredControls.push(i);
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
        this.registeredControls.forEach(i => this.keyValuesForm.removeAt(i));
        this.registeredControls = [];
    }

    // CVA
    onTouched: () => void = () => {
    };

    propagateChange = (_: any) => {
    };

    writeValue(val: any): void {
        val && this.keyValuesForm.setValue(val, {emitEvent: false});
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
}
