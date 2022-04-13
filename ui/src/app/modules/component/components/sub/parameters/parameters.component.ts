import { Component, Input, OnInit, OnChanges } from '@angular/core';
import { FormGroup, FormBuilder, FormArray } from '@angular/forms';

import {
    ComponentTask,
    Authorization
} from '@model';
import { LoginService } from '@core/services';


@Component({
    selector: 'chutney-parameters-component',
    templateUrl: './parameters.component.html',
    styleUrls: ['./parameters.component.scss']
})
export class ParametersComponent implements OnInit, OnChanges {

    @Input() parentForm: FormGroup;
    @Input() editableComponent: ComponentTask;

    collapseParam = true;

    Authorization = Authorization;

    constructor(
        private formBuilder: FormBuilder,
        private loginService: LoginService
    ) {
    }

    ngOnInit(): void {

    }

    ngOnChanges(): void {
        const parameters = this.parentForm.controls['parameters'] as FormArray;
        this.clearFormArray(parameters);
        this.editableComponent.parameters.forEach((keyValue) => {
            parameters.push(
                this.formBuilder.group({
                    key: keyValue.key,
                    value: keyValue.value,
                })
            );
        });
        if (!this.loginService.hasAuthorization([Authorization.COMPONENT_WRITE])) {
            this.parentForm.disable();
        }
    }

    addParameters(): void {
        (this.parentForm.controls['parameters'] as FormArray)
            .push(this.formBuilder.group({
                key: '',
                value: ''
            }));
    }

    removeParameters(itemIndex: number): void {
        const parameters = this.parentForm.controls['parameters'] as FormArray;
        parameters.removeAt(itemIndex);
        this.collapseParam = (parameters.length === 0);
    }

    switchCollapseParam() {
        this.collapseParam = !this.collapseParam;
    }

    private clearFormArray(formArray: FormArray): void {
        for (let i=formArray.length; i>=0; i--) {
            formArray.removeAt(i);
        }
    }
}
