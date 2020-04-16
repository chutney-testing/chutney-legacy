import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { TranslateService } from '@ngx-translate/core';
import {
    ComponentTask,
    Implementation,
    KeyValue,
    ListInput,
    MapInput,
    SelectableTags,
    SimpleInput,
    Task
} from '@model';
import { delay } from '@shared/tools/async-utils';
import { combineLatest, Observable, Subject, Subscription } from 'rxjs';
import { DragulaService } from 'ng2-dragula';
import { debounceTime, takeUntil } from 'rxjs/operators';
import { ComponentService } from '@core/services';
import { distinct, flatMap } from '@shared/tools';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
    selector: 'chutney-create-component',
    templateUrl: './create-component.component.html',
    styleUrls: ['./create-component.component.scss']
})
export class CreateComponent implements OnInit, OnDestroy {

    // global
    viewComponent = true;
    actionToEdit: ComponentTask;

    // referential
    tasks: Array<Task> = [];
    componentTasks: Array<ComponentTask> = [];

    // Message
    message: string;

    // const message
    actionUpdatedMessage;
    actionCreatedMessage;

    // Simple component
    taskFilter: string;
    actionSelected: Task;

    // Complex component
    componentFilter: string;
    editableComponent: ComponentTask;
    componentForm: FormGroup;
    componentTasksCreated: Array<ComponentTask> = [];
    componentsParametersValues_subscriptions: Array<Array<Subscription>> = [];
    executionResult: any;

    collapseScenario = false;
    showChild = false;

    parents: any;

    private unsubscribe$ = new Subject();

    // Tags
    tagData = new SelectableTags<String>();

    private routeParamsSubscription: Subscription;

    constructor(
        private componentService: ComponentService,
        private formBuilder: FormBuilder,
        private dragulaService: DragulaService,
        private translate: TranslateService,
        private route: ActivatedRoute,
        private router: Router
    ) {
    }

    ngOnInit(): void {
        this.initForm();
        this.initTranslation();
        this.initDragAndDrop();

        this.initAllTasksAndComponents().subscribe( (results) => {

            this.tasks = results[0];
            this.componentTasks = results[1];

            this.setSelectedTags();
            this.routeParamsSubscription = this.route.params.subscribe((params) => {
                this.initSelectedComponent(params['id']);
            });
        });
    }

    private initForm() {
        this.componentForm = this.formBuilder.group({
            name: ['', Validators.required],
            parameters: this.formBuilder.array([]),
            componentsParametersValues: this.formBuilder.array([]),
            tags: '',
            strategy: new FormControl()
        });
    }

    private initTranslation() {
        this.translate.get('components.action.messages.update').subscribe((res: string) => {
            this.actionUpdatedMessage = res;
        });
        this.translate.get('components.action.messages.create').subscribe((res: string) => {
            this.actionCreatedMessage = res;
        });
    }

    private initDragAndDrop() {
        this.dragulaService.dragend('COPYABLE')
            .pipe(
                takeUntil(this.unsubscribe$)
            )
            .subscribe(
                () => this.initComponentTasksValues()
            );

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

    private initAllTasksAndComponents(): Observable<any> {
        return combineLatest(
            this.componentService.findAllTasks(),
            this.componentService.findAllComponent()
        ).pipe(
            takeUntil(this.unsubscribe$)
        );
    }

    private setSelectedTags() {
        this.tagData.initialize(this.findAllTags());
    }

    private findAllTags() {
        return distinct(flatMap(this.componentTasks, (sc) => sc.tags)).sort();
    }

    initSelectedComponent(componentId) {
        if (componentId !== null) {
            const foundComponent = this.componentTasks.find(c => c.id === componentId);
            if (foundComponent !== undefined) {
                this.editComponentTask(foundComponent);
            }
        }
    }

    ngOnDestroy() {
        this.dragulaService.destroy('COPYABLE');
        this.unsubscribe$.complete();
        if (this.routeParamsSubscription) {
            this.routeParamsSubscription.unsubscribe();
        }
    }

    initNewComponent() {
        this.setComponentForm(new ComponentTask('', null, [], [], [], [], null));
    }

    editComponentTask(componentToEdit: ComponentTask) {
        this.resetData();

        if (componentToEdit.implementation === null) {
            this.componentService.findParents(componentToEdit.id).subscribe(
                (res) => { this.parents = res; }
            );

            this.setComponentForm(componentToEdit);
        } else {
            this.editableComponent = null;
            this.componentTasksCreated = [];
            this.actionToEdit = componentToEdit;
        }
        this.router.navigateByUrl(`/component/${componentToEdit.id}`);
    }

    save() {
        this.saveComponent(this.createComponent());
    }

    private createComponent(): ComponentTask {
        return new ComponentTask(
            this.componentForm.value['name'],
            null,
            this.componentTasksCreated,
            this.componentForm.value['parameters'].map((p) => new KeyValue(p.key, p.value)),
            [],
            this.componentForm.value['tags'].split(','),
            this.componentForm.value['strategy'],
            this.editableComponent.id
        );
    }

    deleteComponent(id: string) {
        this.componentService.delete(id).subscribe(
            () => {
                this.refreshComponents();
                this.resetData();
                this.editableComponent = null;
            },
            (error) => console.log(error)
        );
    }

    removeStep(index: number) {
        this.componentTasksCreated.splice(index, 1);
        const componentsParametersValues = this.componentForm.controls.componentsParametersValues as FormArray;
        componentsParametersValues.removeAt(index);

        this.clearComponentsParametersValuesSubscriptions(index);
        for (let componentIndex=index; componentIndex<this.componentTasksCreated.length; componentIndex++) {
            const componentTask = this.componentTasksCreated[componentIndex];
            const componentTaskParameters_sub = [];
            componentTask.dataSet.forEach((parameter, parameterIndex) => {
                const ctrl = (componentsParametersValues.controls[componentIndex] as FormArray).controls[parameterIndex] as FormControl;
                const crtl_subscription: Subscription = this.setComponentsParametersValuesSubscriptions(ctrl, componentIndex, parameterIndex);
                componentTaskParameters_sub.push(crtl_subscription);
            });
            this.componentsParametersValues_subscriptions.push(componentTaskParameters_sub);
        }
    }

    resetData() {
        this.actionSelected = null;
        this.actionToEdit = null;
        this.collapseScenario = false;
        this.executionResult = null;
        this.showChild = false;
        this.message = null;

        this.router.navigateByUrl(`/component`);
    }

    execute(environment: string) {
        this.componentService.execute(this.editableComponent, environment).subscribe(
            (res) => { this.executionResult = res; },
            (error) => { this.executionResult = error; }
        );
    }

    /////////// Action
    select(task: Task) {
        this.resetData();

        this.actionSelected = task;

        const mapImpl = task.inputs.filter(i => i.type === 'java.util.Map').map(i => {
            return new MapInput(i.name, []);
        });

        const listImpl = task.inputs.filter(i => i.type === 'java.util.List').map(i => {
            return new ListInput(i.name, []);
        });

        const simpleImpl = task.inputs.filter(i => i.type !== 'java.util.Map' && i.type !== 'java.util.List').map(i => {
            return new SimpleInput(i.name, '', i.type);
        });

        const implementation = new Implementation(task.identifier, '', task.target, mapImpl, listImpl, simpleImpl, []);
        this.actionToEdit = new ComponentTask('', implementation, [], [], [], [], null);
    }

    cancel() {
        this.resetData();
        this.fillFormValuesWith(/*nothing*/);
    }

    closeExecutionPanel() {
        this.executionResult = null;
    }

    private refreshComponents(id?: string): void {
        this.componentService.findAllComponent().subscribe(
            (res) => {
                this.componentTasks = res;
                this.setSelectedTags();
                if (id) {
                    this.router.navigateByUrl(`/component/${id}`).then(() => {
                        this.viewComponent = true;
                        this.showMessage();
                    });
                }
            },
            (error) => console.log(error)
        );
    }

    private fillFormValuesWith(selectedComponent?: ComponentTask) {
        this.editableComponent = null;
        this.componentTasksCreated = [];
        this.componentForm.reset();

        if (selectedComponent === undefined) {
            this.componentForm.controls.name.reset();
            this.componentForm.controls.strategy.reset();
            this.componentForm.controls.tags.reset();
        } else {
            this.editableComponent = selectedComponent;
            this.componentTasksCreated = this.editableComponent.children;

            this.componentForm.controls.name.patchValue(this.editableComponent.name);
            (this.componentForm.controls.tags as FormControl).patchValue(this.editableComponent.tags.join(', '));
        }

        this.clearFormArray(this.componentForm.controls.parameters as FormArray);
        this.clearFormArray(this.componentForm.controls.componentsParametersValues as FormArray);
        this.clearComponentsParametersValuesSubscriptions();
    }

    clearFormArray (formArray: FormArray) {
        while (formArray.length !== 0) {
            formArray.removeAt(0);
        }
    }

    private setComponentForm(selectedComponent: ComponentTask) {
        this.fillFormValuesWith(selectedComponent);
        this.initComponentTasksValues();
    }

    private initComponentTasksValues() {
        const componentsParametersValues = this.componentForm.controls.componentsParametersValues as FormArray;
        this.clearFormArray(componentsParametersValues);
        this.clearComponentsParametersValuesSubscriptions();

        this.componentTasksCreated.forEach((componentTask, componentIndex) => {
            const componentTaskParameters = this.formBuilder.array([]);
            const componentTaskParameters_sub = [];
            componentTask.dataSet.forEach((parameter, parameterIndex) => {
                const ctrl = this.formBuilder.control(parameter.value) as FormControl;
                const crtl_subscription: Subscription = this.setComponentsParametersValuesSubscriptions(ctrl, componentIndex, parameterIndex);
                componentTaskParameters.push(ctrl);
                componentTaskParameters_sub.push(crtl_subscription);
            });
            componentsParametersValues.push(componentTaskParameters);
            this.componentsParametersValues_subscriptions.push(componentTaskParameters_sub);
        });
    }

    private setComponentsParametersValuesSubscriptions(ctrl: FormControl, componentIndex, parameterIndex) {
        const componentsParametersValues = this.componentForm.controls.componentsParametersValues as FormArray;
        return ctrl.valueChanges.pipe(
            debounceTime(250)
        ).subscribe(() => {
            if (this.componentTasksCreated[componentIndex] &&
                componentsParametersValues.get(componentIndex.toString()).get(parameterIndex.toString())) {
                this.componentTasksCreated[componentIndex].dataSet[parameterIndex].value =
                    componentsParametersValues.get(componentIndex.toString()).get(parameterIndex.toString()).value;
            }
        });
    }

    private clearComponentsParametersValuesSubscriptions(fromIndex: number = 0): void {
        for (let i=fromIndex; i<this.componentsParametersValues_subscriptions.length; i++) {
            this.componentsParametersValues_subscriptions[i].forEach(sub => sub.unsubscribe());
        }
        this.componentsParametersValues_subscriptions.splice(fromIndex, this.componentsParametersValues_subscriptions.length);
    }

    saveComponent(componentTask: ComponentTask) {
        this.componentService.save(componentTask)
            .subscribe(
                (id) => {
                    if (this.editableComponent) {
                        this.editableComponent.id = id;
                    }
                    this.refreshComponents(id);
                },
                (error) => console.log(error)
            );
    }

    showMessage() {
        (async () => {
            this.message = 'Saved';
            await delay(3000);
            this.message = null;
        })();
    }

    seeChild() {
        this.showChild = !this.showChild;
    }

    isSelectAll() {
        return this.tagData.isSelectAll();
    }

    selectAll() {
        this.tagData.selectAll();
    }

    unSelectAll() {
        this.tagData.unSelectAll();
    }

    toggleNoTag() {
        this.tagData.toggleNoTag();
    }

    toggleTagSelect(tag: any) {
        this.tagData.toggleSelect(tag);
    }

}
