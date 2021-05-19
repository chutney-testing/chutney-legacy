import { Component, forwardRef, Input, OnChanges } from '@angular/core';
import {
    AbstractControl,
    ControlValueAccessor,
    FormBuilder,
    FormControl,
    FormGroup,
    NG_VALIDATORS,
    NG_VALUE_ACCESSOR,
    ValidationErrors
} from '@angular/forms';
import { of } from 'rxjs';
import { ParameterDefinition, Strategy, StrategyDefinition } from '@model';

@Component({
    selector: 'chutney-strategy-form',
    templateUrl: './strategy-form.component.html',
    styleUrls: ['./strategy-form.component.scss'],
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => StrategyFormComponent),
            multi: true
        },
        {
            provide: NG_VALIDATORS,
            useExisting: forwardRef(() => StrategyFormComponent),
            multi: true
        }
    ]
})
export class StrategyFormComponent implements OnChanges, ControlValueAccessor {

    @Input() strategy: Strategy;

    strategyDefinitions: StrategyDefinition[];
    strategyForm: FormGroup;

    selectedStrategyDef: StrategyDefinition;
    parameterValues: Object;

    // services
    getStrategies(): StrategyDefinition[] {
        return [
            new StrategyDefinition('Default', [], true),
            new StrategyDefinition('Retry',
                [ new ParameterDefinition('timeout', 'duration'), new ParameterDefinition('delay', 'duration') ],
                false),
            new StrategyDefinition('Soft', [], false)
        ];
    }
    // # End of services

    constructor(private formBuilder: FormBuilder) {
        this.strategyForm = this.formBuilder.group({
            type: String,
            parameters: new FormControl()
        });

        of(this.getStrategies()).subscribe(serverStrategies => {
            this.strategyDefinitions = serverStrategies;
        });

    }

    // OnChanges
    ngOnChanges(): void {
        if (this.strategy === undefined || this.strategy === null ) {
            this.strategy = new Strategy(this.strategyDefinitions.find(s => s.isDefault === true ).type, {});
        }

        // patch with initial value
        this.strategyForm.controls.type.patchValue(this.strategy.type);
        this.setSelectedStrategy();
    }

    setSelectedStrategy() {
        this.selectedStrategyDef = this.strategyDefinitions.find(s => s.type === this.strategyForm.get('type').value);
        this.parameterValues = (this.strategy.type === this.selectedStrategyDef.type) ? this.strategy.parameters : {};
    }

    // CVA
    onTouched: () => void = () => {};
    propagateChange = (_: any) => {};

    writeValue(val: any): void {
        val && this.strategyForm.setValue(val, { emitEvent: false });
    }

    registerOnChange(fn: any): void {
        this.propagateChange = fn;
        if (this.strategyForm.get('type').value !== null) {
            this.propagateChange(new Strategy(this.selectedStrategyDef.type, this.parameterValues));
        }
        this.strategyForm.valueChanges.subscribe(fn);
    }

    registerOnTouched(fn: any): void {
        this.onTouched = fn;
    }

    setDisabledState(isDisabled: boolean): void {
        isDisabled ? this.strategyForm.disable() : this.strategyForm.enable();
    }

    validate(c: AbstractControl): ValidationErrors | null {
        return this.strategyForm.valid ? null : { invalidForm: {valid: false, message: 'strategy fields are invalid'}};
    }

}
