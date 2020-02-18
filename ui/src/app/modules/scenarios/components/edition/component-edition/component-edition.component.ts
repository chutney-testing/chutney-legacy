import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { Subscription, combineLatest } from 'rxjs';
import { DragulaService } from 'ng2-dragula';
import { debounceTime } from 'rxjs/operators';

import { FormGroup, FormArray, FormBuilder, FormControl } from '@angular/forms';
import { ComponentTask, KeyValue, ScenarioComponent } from '@model';
import {ComponentService} from '@core/services';
import { newInstance } from '@shared/tools/array-utils';
import { CanDeactivatePage } from '@core/guards';

@Component({
    selector: 'chutney-component-edition',
    templateUrl: './component-edition.component.html',
    styleUrls: ['./component-edition.component.scss']
})
export class ComponentEditionComponent extends CanDeactivatePage implements OnInit, OnDestroy {

    scenarioComponent: ScenarioComponent = new ScenarioComponent();

    componentRefTasksArray: Array<ComponentTask> = [];
    componentFilter: string;
    componentForm: FormGroup = this.formBuilder.group({
        parameters: this.formBuilder.array([]),
        componentsValues: this.formBuilder.array([]),
        tags: ''
    });
    componentTasksCreated: Array<ComponentTask> = [];

    collapseParam = true;
    collapseComponentsParameters: Array<boolean> = [];

    constructor(private componentService: ComponentService,
                private router: Router,
                private route: ActivatedRoute,
                private dragulaService: DragulaService,
                private formBuilder: FormBuilder
    ) {
        super();
    }

    private loadSubscription: Subscription;
    private dragEndSubscription: Subscription;

    ngOnInit() {
        this.initDragAndDrop();
        this.loadSubscription = combineLatest(
            this.componentService.findAllComponent(),
            this.route.params
        ).subscribe(
            results => {
                this.componentRefTasksArray = results[0];
                const duplicate = this.route.snapshot.queryParamMap.get('duplicate');
                if (duplicate) {
                    this.load(results[1]['id'], true);
                } else {
                    this.load(results[1]['id'], false);
                }
            });
    }

    ngOnDestroy() {
        this.loadSubscription.unsubscribe();
        this.dragulaService.destroy('COPYABLE');
        this.dragEndSubscription.unsubscribe();
    }

    canDeactivatePage(): boolean {
        return true;
    }

    save() {
        this.scenarioComponent.componentSteps = this.componentTasksCreated;
        this.updateScenarioParameters();
        let tags = this.componentForm.value['tags'] + '';
        this.scenarioComponent.tags = tags.length != 0 ? tags.split(',') : [];

        // Call service
        this.componentService.saveComponentTestCase(this.scenarioComponent).subscribe(
            (response) => {
                this.router.navigateByUrl('/scenario/' + response + '/execution/last')
                    .then(null);
            },
            (error) => {
                console.log(error);
                this.scenarioComponent.dataSet = [];
            }
        );
    }

    cancel() {
        if (this.scenarioComponent.id !== null) {
            this.router.navigateByUrl('/scenario/' + this.scenarioComponent.id + '/execution/last');
        } else {
            this.router.navigateByUrl('/scenario/');
        }
    }

    addParameters(): void {
        this.collapseParam = false;
        (this.componentForm.controls.parameters as FormArray)
            .push(this.formBuilder.group({
                key: '',
                value: ''
            }));
    }

    removeParameter(index: number): void {
        const parameters = this.componentForm.controls.parameters as FormArray;
        parameters.removeAt(index);
        this.collapseParam = (parameters.length === 0);
    }

    removeComponent(index: number) {
        this.componentTasksCreated.splice(index, 1);
        this.collapseComponentsParameters.splice(index, 1);
        (this.componentForm.controls.componentsValues as FormArray).removeAt(index);
    }

    switchCollapseParam() {
        this.collapseParam = !this.collapseParam;
    }

    switchCollapseComponentsParameters(index: number) {
        this.collapseComponentsParameters[index] = !this.collapseComponentsParameters[index];
        this.collapseComponentsParameters = newInstance(this.collapseComponentsParameters);
    }

    private initDragAndDrop() {
        this.dragEndSubscription = this.dragulaService.dragend('COPYABLE').subscribe(() => {
            this.initFormComponentRefsParametersValues();
        });
        this.dragulaService.createGroup('COPYABLE', {
            copy: (el, source) => {
                return source.id === 'left';
            },
            copyItem: (componentTask: ComponentTask): any => {
                return componentTask.clone();
            },
            accepts: (el, target, source, sibling) => {
                // To avoid dragging from right to left container
                return target.id !== 'left';
            }
        });
    }

    private load(id, duplicate: boolean) {
        if (id !== undefined) {
            this.componentService.findComponentTestCase(id).subscribe(
                (componentScenario) => {
                    this.scenarioComponent = componentScenario;
                    componentScenario.componentSteps.forEach((componentTask: ComponentTask) => {
                        this.componentTasksCreated.push(componentTask.clone());
                        this.collapseComponentsParameters = this.componentTasksCreated.map(() => true);
                    });
                    this.initFormComponentParameters();
                    this.initFormComponentRefsParametersValues();
                    this.componentForm.controls['tags'].setValue(this.scenarioComponent.tags);

                    if (duplicate) {
                        this.scenarioComponent.id = null;
                        this.scenarioComponent.creationDate = null;
                        this.scenarioComponent.title = '--COPY-- ' + this.scenarioComponent.title;
                    }
                },
                (error) => {
                    console.log(error);
                }
            );
        }
    }

    private initFormComponentRefsParametersValues() {
        this.collapseComponentsParameters = [];
        this.componentForm.controls.componentsValues = this.formBuilder.array([]);
        const componentsValues = this.componentForm.controls.componentsValues as FormArray;

        this.componentTasksCreated.forEach((componentTask, componentIndex) => {
            const componentTastParameters = this.formBuilder.array([]);
            componentTask.dataSet.forEach((parameter, parameterIndex) => {
                const ctrl = this.formBuilder.control(parameter.value) as FormControl;
                ctrl.valueChanges.pipe(
                    debounceTime(500)
                ).subscribe(() => {
                    this.componentTasksCreated[componentIndex].dataSet[parameterIndex].value =
                        componentsValues.get(componentIndex.toString()).get(parameterIndex.toString()).value;
                });
                componentTastParameters.push(ctrl);
            });
            componentsValues.push(componentTastParameters);

            this.collapseComponentsParameters.push(true);
        });
    }

    private initFormComponentParameters() {
        const parameters = this.componentForm.controls.parameters as FormArray;
        this.scenarioComponent.parameters.forEach((keyValue) => {
            parameters.push(
                this.formBuilder.group({
                    key: keyValue.key,
                    value: keyValue.value,
                })
            );
        });
    }

    private updateScenarioParameters() {
        this.scenarioComponent.parameters = [];
        const parameters = this.componentForm.controls.parameters as FormArray;
        for (let i = 0; i < parameters.length; i++) {
            const parameter = parameters.get(i.toString()) as FormGroup;
            if (parameter.get('key').value !== '') {
                this.scenarioComponent.parameters.push(new KeyValue(parameter.get('key').value, parameter.get('value').value));
            }
        }
    }

}
