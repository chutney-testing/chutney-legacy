import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { combineLatest, Observable, Subject, Subscription } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { DragulaService } from 'ng2-dragula';

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
import { ComponentService } from '@core/services';
import { distinct, flatMap } from '@shared/tools';

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
    messageType: string;

    // const message
    private savedMessage;

    // Simple component
    taskFilter: string;
    actionSelected: Task;

    // Complex component
    componentFilter: string;
    editableComponent: ComponentTask;
    componentForm: FormGroup;
    componentTasksCreated: Array<ComponentTask> = [];
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

        this.initAllTasksAndComponents().subscribe((results) => {

            this.tasks = results[0];
            this.componentTasks = results[1];

            this.setSelectedTags();
            this.routeParamsSubscription = this.route.params.pipe(
                takeUntil(this.unsubscribe$)
            ).subscribe((params) => {
                this.initSelectedComponent(params['id']);
            });
        });
    }

    private initForm() {
        this.componentForm = this.formBuilder.group({
            name: ['', Validators.required],
            parameters: this.formBuilder.array([]),
            tags: '',
            strategy: new FormControl()
        });
    }

    private initTranslation() {
        this.translate.get('global.actions.done.saved').subscribe((res: string) => {
            this.savedMessage = res;
        });
    }

    private initDragAndDrop() {
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
    }

    initNewComponent() {
        this.fillFormValuesWith(new ComponentTask('', null, [], [], [], [], null));
    }

    editComponentTask(componentToEdit: ComponentTask) {
        this.resetData();

        if (componentToEdit.implementation === null) {
            this.componentService.findParents(componentToEdit.id).subscribe(
                (res) => { this.parents = res; }
            );
            this.fillFormValuesWith(componentToEdit);
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

    duplicateComponent() {
        this.editableComponent = Object.assign({}, this.editableComponent);
        this.editableComponent.id = null;
        this.componentForm.controls.name.patchValue('--COPY-- ' + this.editableComponent.name);
        this.router.navigateByUrl(`/component/list`);
    }

    duplicateAction() {
        this.router.navigateByUrl(`/component/list`);
    }

    removeStep(index: number) {
        this.componentTasksCreated.splice(index, 1);
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

        const implementation = new Implementation(task.identifier, '', task.target, mapImpl, listImpl, simpleImpl, [], []);
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
                        this.showMessage(this.savedMessage);
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
    }

    clearFormArray(formArray: FormArray) {
        while (formArray.length !== 0) {
            formArray.removeAt(0);
        }
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
                (err) => this.showMessage(err.error, true)
            );
    }

    showMessage(message: string, error: boolean = false) {
        (async () => {
            this.messageType = error ? 'danger' : 'info';
            this.message = message;
            await delay(3000);
            this.message = null;
            this.messageType = null;
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
        this.tagData.deselectAll();
    }

    toggleNoTag() {
        this.tagData.toggleNoTag();
    }

    toggleTagSelect(tag: any) {
        this.tagData.toggleSelect(tag);
    }

}
