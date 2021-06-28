import { Component, forwardRef, Input, OnChanges } from '@angular/core';
import {
    AbstractControl,
    ControlValueAccessor,
    FormBuilder,
    FormGroup,
    NG_VALIDATORS,
    NG_VALUE_ACCESSOR,
    ValidationErrors,
    Validators
} from '@angular/forms';
import { ParameterDefinition } from '@model';
import { durationValidator } from '@shared/validators/duration.validator';

@Component({
    selector: 'chutney-strategy-parameters-form',
    templateUrl: './strategy-parameter-form.component.html',
    styleUrls: ['./strategy-parameter-form.component.scss'],
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => StrategyParameterFormComponent),
            multi: true
        },
        {
            provide: NG_VALIDATORS,
            useExisting: forwardRef(() => StrategyParameterFormComponent),
            multi: true
        }
    ]
})
export class StrategyParameterFormComponent implements OnChanges, ControlValueAccessor {

    @Input() parameters: ParameterDefinition[] = [];
    @Input() values: Object;

    parameterForm: FormGroup;

    private registeredControls: string[] = [];

    constructor(private fb: FormBuilder) {
        this.parameterForm = this.fb.group({});
        this.createParametersForm();
    }

    // OnChanges
    ngOnChanges(): void {
        this.createParametersForm();
    }

    private createParametersForm() {
        const disabledSave = this.parameterForm.disabled;
        this.clearForm();
        this.parameters.forEach(p => {
            this.parameterForm.addControl(
                p.name,
                this.fb.control(this.findValue(p.name, this.values), this.findValidatorFor(p.type))
            );
            this.registeredControls.push(p.name);
        });
        this.setDisabledState(disabledSave);
    }

    private findValidatorFor(type: string) {
        switch (type) {
            case 'duration':
                return durationValidator();
            default:
                return Validators.nullValidator;
        }
    }

    private clearForm() {
        this.registeredControls.forEach( name => this.parameterForm.removeControl(name) );
        this.registeredControls = [];
    }

    private findValue(name: string, values: Object): any {
        if (values === undefined) {
            return '';
        }

        const kv: [string, any][] = Object.entries(values);
        if (kv.length === 0) {
            return '';
        }

        return kv.find(t => t.find(x => true) === name)[1];
    }

    // CVA
    onTouched: () => void = () => {};

    propagateChange = (_: any) => {};

    writeValue(val: any): void {
        val && this.parameterForm.setValue(val, { emitEvent: false });
    }

    registerOnChange(fn: any): void {
        this.propagateChange = fn;
        this.parameterForm.valueChanges.subscribe(fn);
    }

    registerOnTouched(fn: any): void {
        this.onTouched = fn;
    }

    setDisabledState?(isDisabled: boolean): void {
        isDisabled ? this.parameterForm.disable() : this.parameterForm.enable();
    }

    validate(c: AbstractControl): ValidationErrors | null {
        return this.parameterForm.valid ? null : {
            invalidForm: {
                valid: false,
                message: 'retry strategy fields are invalid'
            }
        };
    }
}
