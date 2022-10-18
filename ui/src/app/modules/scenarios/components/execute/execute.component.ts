import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { FormGroup, FormBuilder, Validators, FormArray } from '@angular/forms';
import { Observable, Subscription } from 'rxjs';
import { delay, map, tap } from 'rxjs/operators';

import { TestCase, KeyValue } from '@model';
import {  ComponentService, ScenarioService } from '@core/services';
import { ScenarioExecutionService } from '@modules/scenarios/services/scenario-execution.service';

@Component({
    selector: 'chutney-execute',
    templateUrl: './execute.component.html',
    styleUrls: ['./execute.component.scss']
})
export class ExecuteComponent implements OnInit, OnDestroy {

    testCase$: Observable<TestCase>;
    private computedParameters: Array<KeyValue>;
    private testCaseId: string;

    componentForm: FormGroup;
    env: string;
    private isComposed = TestCase.isComposed;
    private routeParamsSubscription: Subscription;

    constructor(private scenarioExecutionService: ScenarioExecutionService,
                private componentService: ComponentService,
                private scenarioService: ScenarioService,
                private formBuilder: FormBuilder,
                private route: ActivatedRoute,
                private router: Router) {
    }

    ngOnInit() {
        this.routeParamsSubscription = this.route.params.subscribe((params) => {
            this.loadScenario(params['id']);
            this.env = params['env'];
        });
    }

    ngOnDestroy(): void {
        if (this.routeParamsSubscription) {
            this.routeParamsSubscription.unsubscribe();
        }
    }

    execute(event: Event) {
        (event.currentTarget as HTMLButtonElement).disabled = true;
        this.scenarioExecutionService.executeScenarioAsync(this.testCaseId, this.buildDataSetFromForm(), this.env)
            .pipe(
                delay(1000)
            )
            .subscribe(
            executionId =>
                this.router.navigateByUrl(`/scenario/${this.testCaseId}/executions/${executionId}`)
                    .then(null),
            error =>
                this.router.navigateByUrl(`/scenario/${this.testCaseId}/executions/last`)
                    .then(null)
        );
    }

    private loadScenario(testCaseId: string) {
        this.testCaseId = testCaseId;

        let tmp$: Observable<TestCase>;
        if (this.isComposed(testCaseId)) {
            tmp$ = this.componentService.findComponentTestCase(testCaseId).pipe(
                map(sc => TestCase.fromComponent(sc))
            );
        } else {
            tmp$ = this.scenarioService.findRawTestCase(testCaseId);
        }

        this.testCase$ = tmp$.pipe(tap(tc => this.initComponentForm(tc.computedParameters)));
    }

    private initComponentForm(computedParams: Array<KeyValue>) {
        this.computedParameters = computedParams;

        this.componentForm = this.formBuilder.group({
            parameters: this.formBuilder.array([])
        });

        const parameters = this.componentForm.controls['parameters'] as FormArray;
        computedParams.forEach((keyValue) => {
            parameters.push(
                this.formBuilder.control(keyValue.value, Validators.required)
            );
        });
    }

    private buildDataSetFromForm(): Array<KeyValue> {
        const computedParameters: Array<KeyValue> = [];
        const parameters = this.componentForm.controls['parameters'] as FormArray;
        parameters.controls.forEach((ctlr, i) => {
            computedParameters.push(new KeyValue(this.computedParameters[i].key, ctlr.value));
        });
        return computedParameters;
    }

}
