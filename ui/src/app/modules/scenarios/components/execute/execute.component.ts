import { Component, OnInit, Input, OnDestroy } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { FormGroup, FormBuilder, Validators, FormArray } from '@angular/forms';
import { Subscription } from 'rxjs';

import { TestCase, ScenarioComponent, KeyValue } from '@model';
import { ScenarioExecutionService, ComponentService } from '@core/services';
import { delay } from 'rxjs/operators';

@Component({
    selector: 'chutney-execute',
    templateUrl: './execute.component.html',
    styleUrls: ['./execute.component.scss']
})
export class ExecuteComponent implements OnInit, OnDestroy {

    @Input() testCase: TestCase;

    componentForm: FormGroup;
    env: string;
    private routeParamsSubscription: Subscription;

    constructor(private scenarioExecutionService: ScenarioExecutionService,
                private componentService: ComponentService,
                private formBuilder: FormBuilder,
                private route: ActivatedRoute,
                private router: Router) {
    }

    ngOnInit() {
        if (!this.testCase && this.scenarioExecutionService.testCaseToExecute) {
            this.testCase = this.scenarioExecutionService.testCaseToExecute;
            this.initComponentForm();
            this.routeParamsSubscription = this.route.params.subscribe((params) => {
                this.env = params['env'];
            });
        } else {
            this.routeParamsSubscription = this.route.params.subscribe((params) => {
                this.loadScenario(params['id']);
                this.env = params['env'];
            });
        }
    }

    ngOnDestroy(): void {
        if (this.routeParamsSubscription) {
            this.routeParamsSubscription.unsubscribe();
        }
        this.scenarioExecutionService.testCaseToExecute = null;
    }

    execute() {
        this.scenarioExecutionService.executeScenarioAsync(this.testCase.id, this.buildDataSetFromForm(), this.env)
            .pipe(
                delay(1000)
            )
            .subscribe(
            executionId =>
                this.router.navigateByUrl(`/scenario/${this.testCase.id}/execution/${executionId}`)
                    .then(null),
            error =>
                this.router.navigateByUrl(`/scenario/${this.testCase.id}/execution/last`)
                    .then(null)
        );
    }

    private loadScenario(testCaseId: string) {
        this.componentService.findComponentTestCase(testCaseId).subscribe((testCase: ScenarioComponent) => {
            this.testCase = TestCase.fromComponent(testCase);
            this.initComponentForm();
        });
    }

    private initComponentForm() {
        this.componentForm = this.formBuilder.group({
            parameters: this.formBuilder.array([])
        });

        const parameters = this.componentForm.controls.parameters as FormArray;
        this.testCase.dataSet.forEach((keyValue) => {
            parameters.push(
                this.formBuilder.control(keyValue.value, Validators.required)
            );
        });
    }

    private buildDataSetFromForm(): Array<KeyValue> {
        let dataset: Array<KeyValue> = [];
        const parameters = this.componentForm.controls.parameters as FormArray;
        parameters.controls.forEach((ctlr, i) => {
            dataset.push(new KeyValue(this.testCase.dataSet[i].key, ctlr.value))
        })
        return dataset;
    }

}
