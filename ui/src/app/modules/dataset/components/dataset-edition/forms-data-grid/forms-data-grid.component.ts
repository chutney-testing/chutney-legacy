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
    selector: 'chutney-forms-data-grid',
    templateUrl: './forms-data-grid.component.html',
    styleUrls: ['./forms-data-grid.component.scss'],
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => FormsDataGridComponent),
            multi: true
        },
        {
            provide: NG_VALIDATORS,
            useExisting: forwardRef(() => FormsDataGridComponent),
            multi: true
        }
    ]
})
export class FormsDataGridComponent implements OnChanges, ControlValueAccessor {

    @Input() dataGrid: Array<Array<KeyValue>> = [];

    dataGridForm: FormArray;
    headers: Array<string> = [];

    constructor(private fb: FormBuilder) {
        this.dataGridForm = this.fb.array([]);
    }

    // OnChanges
    ngOnChanges(): void {
        this.createForm();
    }

    private createForm() {
        this.createTable();
        this.createReactiveForm();
    }

    private createTable() {
        this.headers = this.getHeaders();
    }

    private getHeaders(): Array<string> {
        if (this.dataGrid.length > 0) {
            return this.dataGrid[0].map(v => v.key);
        }
        return [];
    }

    private createReactiveForm() {
        let i = 0;
        this.dataGrid.forEach(line => {
            let lineControls = this.fb.array([]);
            let j = 0;
            line.forEach(cell => {
                lineControls.insert(
                    j,
                    this.fb.control(cell)
                );
                j++;
            });

            this.dataGridForm.insert(
                i,
                lineControls
            );
            i++;
        });
    }

    addColumn() {
        this.addTableColumn();
        this.addFormColumn();

    }

    private addTableColumn() {
        this.headers.push('');
        this.dataGrid.forEach(line => {
            line.push(new KeyValue('', ''));
        });
    }

    private addFormColumn() {
        this.dataGridForm.controls.forEach((lineControl: FormArray) => {
            let cellControl = this.fb.control(new KeyValue('', ''));
            lineControl.push(cellControl);
        });
    }

    removeColumn(i: number) {
        this.removeTableColumn(i);
        this.removeFormColumn(i);
    }

    private removeTableColumn(i: number) {
        this.headers.splice(i, 1);
        this.dataGrid.forEach(l => {
            l.splice(i, 1);
        })
    }

    private removeFormColumn(i: number) {
        this.dataGridForm.controls.forEach((lineControl: FormArray) => {
            lineControl.removeAt(i);
        });
    }

    addLine() {
        this.addTableLine();
        this.addFormLine();
    }

    private addTableLine() {
        this.dataGrid[this.dataGrid.length] = [];
        this.headers.forEach(() => {
            this.dataGrid[this.dataGrid.length - 1].push(new KeyValue('', ''));
        });
    }

    private addFormLine() {
        let lineControls = this.fb.array([]);
        let i = 0;
        this.headers.forEach(header => {
            lineControls.insert(
                i,
                this.fb.control(new KeyValue(header, ''))
            );
            i++;
        });
        this.dataGridForm.push(lineControls);
    }

    removeLine(i: number) {
        this.removeTableLine(i);
        this.removeFormLine(i);
    }

    private removeTableLine(i: number) {
        this.dataGrid.splice(i, 1);
    }

    private removeFormLine(i: number) {
        this.dataGridForm.removeAt(i);
    }

    updateHeader(i: number, newHeader: string) {
        const lines = this.dataGridForm.controls;
        lines.forEach( (line: FormArray ) => {
            let cell = line.controls[i];
            cell.patchValue(new KeyValue(newHeader, cell.value.value));
        });
    }

    updateValue(i: number, j: number, newValue: string) {
        const lineControl = this.dataGridForm.controls[i] as FormArray;
        const cell = lineControl.controls[j];
        cell.patchValue(new KeyValue(cell.value.key, newValue));
    }

    // CVA
    onTouched: () => void = () => {
    };

    propagateChange = (_: any) => {
    };

    writeValue(val: any): void {
        val && this.dataGridForm.setValue(val, {emitEvent: false});
    }

    registerOnChange(fn: any): void {
        this.propagateChange = fn;
        this.dataGridForm.valueChanges.subscribe(fn);
    }

    registerOnTouched(fn: any): void {
        this.onTouched = fn;
    }

    setDisabledState?(isDisabled: boolean): void {
        isDisabled ? this.dataGridForm.disable() : this.dataGridForm.enable();
    }

    validate(c: AbstractControl): ValidationErrors | null {
        return this.dataGridForm.valid ? null : {
            invalidForm: {
                valid: false,
                message: 'fields are invalid'
            }
        };
    }
}
