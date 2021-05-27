import { Component, EventEmitter, Input, OnChanges, Output } from '@angular/core';
import { FormArray, FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';

import { ComponentTask, Implementation, KeyValue, ListInput, MapInput, SimpleInput, Target, Task } from '@model';
import { ComponentService, EnvironmentAdminService } from '@core/services';
import { newInstance } from '@shared/tools/array-utils';


@Component({
    selector: 'chutney-action-edit-component',
    templateUrl: './action-edit.component.html',
    styleUrls: ['./action-edit.component.scss']
})
export class ActionEditComponent implements OnChanges {

    @Input() editComponent: ComponentTask;
    @Output() createEvent = new EventEmitter();
    @Output() deleteEvent = new EventEmitter();
    @Output() cancelEvent = new EventEmitter();
    @Output() duplicateEvent = new EventEmitter();

    actionForm: FormGroup;

    availableTargets: Array<Target>;
    task: Task;

    executionResult: any;

    collapseOutputs = true;
    collapseValidations = true;
    collapseInputs = false;
    collapseInputsMap: Array<boolean> = [];
    collapseInputsList: Array<boolean> = [];
    sideBar = false;
    parents: any;

    constructor(
        private formBuilder: FormBuilder,
        private environmentService: EnvironmentAdminService,
        private componentService: ComponentService
    ) {
        this.environmentService.targets().subscribe(
            (res) => { this.availableTargets = res; }
        );

        this.actionForm = this.formBuilder.group({
            name: ['', Validators.required],
            target: '',
            inputs: this.formBuilder.array([]),
            inputsMap: this.formBuilder.array([]),
            inputsList: this.formBuilder.array([]),
            parameters: this.formBuilder.array([]),
            tags: [],
            strategy: new FormControl(),
            outputs: this.formBuilder.array([]),
            validations: this.formBuilder.array([]),
        });
    }

    ngOnChanges(): void {
        this.executionResult = null;
        this.componentService.findAllTasks().subscribe(
            (res) => {
                this.task = res.find(t => t.identifier === this.editComponent.implementation.identifier);
                this.selectAction(this.editComponent);
            },
            (error) => console.log(error)
        );
        if (this.editComponent.id) {
            this.componentService.findParents(this.editComponent.id).subscribe(
                (res) => { this.parents = res; }
            );
        }
    }

    delete() {
        this.deleteEvent.emit(this.editComponent.id);
    }

    duplicate() {
        this.editComponent = Object.assign({}, this.editComponent);
        this.editComponent.id = null;
        this.parents = null;
        this.actionForm.controls.name.patchValue('--COPY-- ' + this.actionForm.value['name']);
        this.duplicateEvent.emit();
    }

    edit() {
        const componentTask = this.createComponentTask();
        this.createEvent.emit(componentTask);
    }

    createComponentTask() {
        const implementation = this.createImplementation();

        return new ComponentTask(
            this.actionForm.value['name'],
            implementation,
            [],
            this.actionForm.value['parameters'].map((p) => new KeyValue(p.key, p.value)),
            [],
            this.actionForm.value['tags'].split(','),
            this.actionForm.value['strategy'],
            this.editComponent.id
        );
    }

    createImplementation(): Implementation {
        const target = this.actionForm.value['target'];
        const mapInputs = this.actionForm.value['inputsMap'];
        const listInputs = this.actionForm.value['inputsList'];
        const simpleInputs = this.actionForm.value['inputs'];
        const outputs = this.actionForm.value['outputs'];
        const validations = this.actionForm.value['validations'];

        const outputsTmp = outputs.map((p) => new KeyValue(p.key, p.value));
        const validationsTmp = validations.map((p) => new KeyValue(p.key, p.value));

        const mapImpl = mapInputs.map(i => {
            const map = new Array<KeyValue>();
            i.valueMap.map(v => map.push(new KeyValue(v.k, v.v)));
            return new MapInput(i.keyMap, map);
        });

        const listImpl = listInputs.map(i => {
            const list = [];
            i.valueList.map(v => {
                let value: any;
                try {
                    value = JSON.parse(v.l);
                } catch (e) {
                    value = v.l;
                }
                list.push(value);
            });
            return new ListInput(i.keyList, list);

        });

        const simpleImpl = simpleInputs.map(i => {
            return new SimpleInput(i.key, i.value);
        });

        return new Implementation(
            this.editComponent.implementation.identifier,
            target,
            this.editComponent.implementation.hasTarget,
            mapImpl,
            listImpl,
            simpleImpl,
            outputsTmp,
            validationsTmp
        );
    }

    cancel() {
        this.cancelEvent.emit();
    }

    execute(environment: string) {
        this.componentService.execute(this.editComponent, environment).subscribe(
            (res) => { this.executionResult = res; },
            (error) => { this.executionResult = error.error; }
        );
    }

    clearFormArray(formArray: FormArray) {
        while (formArray.length !== 0) {
            formArray.removeAt(0);
        }
    }

    fillFormValuesWith(component: ComponentTask) {
        this.actionForm.reset();

        this.actionForm.controls.strategy.reset();
        this.clearFormArray(this.actionForm.controls.inputs as FormArray);
        this.clearFormArray(this.actionForm.controls.inputsMap as FormArray);
        this.clearFormArray(this.actionForm.controls.inputsList as FormArray);
        this.clearFormArray(this.actionForm.controls.parameters as FormArray);
        this.clearFormArray(this.actionForm.controls.outputs as FormArray);
        this.clearFormArray(this.actionForm.controls.validations as FormArray);

        this.actionForm.controls.name.patchValue(component.name);
        this.actionForm.controls.target.patchValue(component.implementation.target);
        this.actionForm.controls.tags.patchValue(component.tags.join(', '));
    }

    selectAction(component: ComponentTask) {
        this.fillFormValuesWith(component);

        // Fill outputs Map
        const outputs = this.actionForm.controls.outputs as FormArray;
        component.implementation.outputs.forEach((keyValue) => {
            outputs.push(
                this.formBuilder.group({
                    key: keyValue.key,
                    value: keyValue.value,
                })
            );
        });

        // Fill validations Map
        const validations = this.actionForm.controls.validations as FormArray;
        component.implementation.validations.forEach((keyValue) => {
            validations.push(
                this.formBuilder.group({
                    key: keyValue.key,
                    value: keyValue.value,
                })
            );
        });

        // Fill inputs Map
        const inputsMap = this.actionForm.controls.inputsMap as FormArray;
        this.collapseInputsMap = [];
        component.implementation.mapInputs.forEach(i => {
            inputsMap.push(this.buildInputMap(i.name, i.values));
            this.collapseInputsMap.push(true);
        });

        // Fill inputs List
        const inputsList = this.actionForm.controls.inputsList as FormArray;
        this.collapseInputsList = [];
        component.implementation.listInputs.forEach(i => {
            inputsList.push(this.buildInputList(i.name, i.values));
            this.collapseInputsList.push(true);
        });

        // Fill inputs others
        const inputs = this.actionForm.controls.inputs as FormArray;
        component.implementation.inputs.forEach(i => {
            const inputType = this.getInputType(i);
            inputs.push(this.formBuilder.group({
                key: i.name,
                value: i.value,
                placeholder: inputType,
                inputType: inputType
            }));
        });
    }

    addMapItem(index: number): void {
        this.collapseInputsMap[index] = false;
        (((this.actionForm.controls.inputsMap as FormArray)
            .controls[index] as FormGroup)
            .controls.valueMap as FormArray)
            .push(this.formBuilder.group({
                k: '',
                v: ''
            }));
    }

    addListItem(index: number): void {
        this.collapseInputsList[index] = false;
        (((this.actionForm.controls.inputsList as FormArray)
            .controls[index] as FormGroup)
            .controls.valueList as FormArray)
            .push(this.formBuilder.group({
                l: ''
            }));
    }

    removeMapItem(index: number, itemIndex: number): void {
        const fa = (((this.actionForm.controls.inputsMap as FormArray)
            .controls[index] as FormGroup)
            .controls.valueMap as FormArray);

        fa.removeAt(itemIndex);
        this.collapseInputsMap[index] = (fa.length === 0);
    }

    removeListItem(index: number, itemIndex: number): void {
        const fa = (((this.actionForm.controls.inputsList as FormArray)
            .controls[index] as FormGroup)
            .controls.valueList as FormArray);

        fa.removeAt(itemIndex);
        this.collapseInputsList[index] = (fa.length === 0);
    }

    addOutput(): void {
        this.collapseOutputs = false;
        (this.actionForm.controls.outputs as FormArray)
            .push(this.formBuilder.group({
                key: '',
                value: ''
            }));
    }

    removeOutput(itemIndex: number): void {
        const outputs = this.actionForm.controls['outputs'] as FormArray;
        outputs.removeAt(itemIndex);
        this.collapseOutputs = (outputs.length === 0);
    }

    addValidation(): void {
        this.collapseValidations = false;
        (this.actionForm.controls.validations as FormArray)
            .push(this.formBuilder.group({
                key: '',
                value: ''
            }));
    }

    removeValidation(itemIndex: number): void {
        const validations = this.actionForm.controls['validations'] as FormArray;
        validations.removeAt(itemIndex);
        this.collapseValidations = (validations.length === 0);
    }

    hasInputs(): boolean {
        const inputs = this.actionForm.controls.inputs as FormArray;
        const inputsMap = this.actionForm.controls.inputsMap as FormArray;
        const inputsList = this.actionForm.controls.inputsList as FormArray;
        return inputs.length > 0 || inputsMap.length > 0 || inputsList.length > 0;
    }

    switchCollapseInputs() {
        this.collapseInputs = !this.collapseInputs;
    }

    switchCollapseOutputs() {
        this.collapseOutputs = !this.collapseOutputs;
    }

    switchCollapseValidations() {
        this.collapseValidations = !this.collapseValidations;
    }

    switchCollapseInputsMap(index: number) {
        this.collapseInputsMap[index] = !this.collapseInputsMap[index];
        this.collapseInputsMap = newInstance(this.collapseInputsMap);
    }

    switchCollapseInputsList(index: number) {
        this.collapseInputsList[index] = !this.collapseInputsList[index];
        this.collapseInputsList = newInstance(this.collapseInputsList);
    }

    switchListToVariableRef(index: number) {
        const name = this.actionForm.value['inputsList'][index].keyList;

        (this.actionForm.controls.inputsList as FormArray)
            .removeAt(index);

        (this.actionForm.controls.inputs as FormArray)
            .push(this.formBuilder.group({
                key: name,
                value: '',
                placeholder: '${#variable_of_type_List}',
                inputType: 'java.util.List'
            }));
    }

    switchMapToVariableRef(index: number) {
        const name = this.actionForm.value['inputsMap'][index].keyMap;

        (this.actionForm.controls.inputsMap as FormArray)
            .removeAt(index);

        (this.actionForm.controls.inputs as FormArray)
            .push(this.formBuilder.group({
                key: name,
                value: '',
                placeholder: '${#variable_of_type_Map}',
                inputType: 'java.util.Map'
            }));
    }

    sideBarToggle() {
        this.sideBar = !this.sideBar;
        if (this.sideBar) {
            this.executionResult = null;
        }
    }

    switchVariableRefToStruct(index: number) {
        const inputElement = this.actionForm.value['inputs'][index];

        if (inputElement.inputType === 'java.util.List') {
            this.switchVariableRefToList(inputElement.key, index);
        } else if (inputElement.inputType === 'java.util.Map') {
            this.switchVariableRefToMap(inputElement.key, index);
        }
    }

    isVariableRef(index: number) {
        const inputType = this.actionForm.value['inputs'][index].inputType;
        return (inputType === 'java.util.List' || inputType === 'java.util.Map');
    }

    closeExecutionPanel() {
        this.executionResult = null;
    }

    private switchVariableRefToList(name: String, index: number) {
        (this.actionForm.controls.inputs as FormArray)
            .removeAt(index);

        (this.actionForm.controls.inputsList as FormArray)
            .push(this.buildInputList(name));
    }

    private switchVariableRefToMap(name: String, index: number) {
        (this.actionForm.controls.inputs as FormArray)
            .removeAt(index);

        (this.actionForm.controls.inputsMap as FormArray)
            .push(this.buildInputMap(name));
    }

    private buildInputList(name: String, values: Array<Object> = []) {
        return this.formBuilder.group({
            keyList: name,
            valueList: this.formBuilder.array(values.map(s => {
                let ss = s;
                if (typeof s !== 'string') {
                    ss = JSON.stringify(s);
                }
                return this.formBuilder.group({
                    l: ss
                });
            })),
        });
    }

    private buildInputMap(name: String, values: Array<KeyValue> = []) {
        return this.formBuilder.group({
            keyMap: name,
            valueMap: this.formBuilder.array(
                Array.from(values).map(keyvalue => this.formBuilder.group({
                    k: keyvalue.key,
                    v: keyvalue.value
                }))),
        });
    }

    private getInputType(input: SimpleInput) {
        if (input.type != null) {
            return input.type;
        }
        return this.task.inputs.find(i => i.name === input.name).type;
    }
}
