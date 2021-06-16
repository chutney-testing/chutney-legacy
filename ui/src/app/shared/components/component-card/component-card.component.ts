import { Component, Input, Output, EventEmitter, OnChanges, SimpleChanges, OnDestroy } from '@angular/core';
import { FormGroup, FormBuilder, FormControl } from '@angular/forms';
import { Subscription } from 'rxjs';
import { debounceTime } from 'rxjs/operators';

import { ComponentTask, Authorization } from '@model';
import { LoginService } from '@core/services';
import { randomIntFromInterval } from '@shared/tools';

@Component({
    selector: 'chutney-component-card',
    templateUrl: './component-card.component.html',
    styleUrls: ['./component-card.component.scss']
})
export class ComponentCardComponent implements OnChanges, OnDestroy {

    @Input() component: ComponentTask;
    @Output() deleteEvent = new EventEmitter();

    cardForm: FormGroup;
    uid: number = randomIntFromInterval(1, 500);
    collapseComponentsParameters = true;

    private parametersValueChangeSubscription: Array<Subscription> = [];

    Authorization = Authorization;

    constructor(
        private formBuilder: FormBuilder,
        private loginService: LoginService
    ) {
    }

    ngOnChanges(changes: SimpleChanges): void {
        this.initForm();
    }

    ngOnDestroy(): void {
        this.cleanParametersSubscription();
    }

    switchCollapseComponentsParameter() {
        this.collapseComponentsParameters = !this.collapseComponentsParameters;
        if (!this.collapseComponentsParameters) {
            this.initForm();
        } else {
            this.cleanParametersSubscription();
        }
    }

    delete() {
        this.deleteEvent.emit();
    }

    private initForm() {
        this.cardForm = this.formBuilder.group({});
        if (this.component && this.component.computedParameters) {
            this.component.computedParameters.forEach((kv, index) => {
                const ctrl: FormControl = this.formBuilder.control(kv.value);
                this.cardForm.addControl(kv.key, ctrl);
                this.parametersValueChangeSubscription.push(
                    ctrl.valueChanges.pipe(
                        debounceTime(250)
                    ).subscribe(v => {
                        this.component.computedParameters[index].value = v;
                    })
                );
            });

            if (!this.loginService.hasAuthorization([Authorization.COMPONENT_WRITE, Authorization.SCENARIO_WRITE])) {
                this.cardForm.disable();
            }
        }
    }

    private cleanParametersSubscription() {
        this.parametersValueChangeSubscription.forEach(s => s.unsubscribe());
    }
}
