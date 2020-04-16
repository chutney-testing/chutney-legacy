import { TestBed, async, ComponentFixture } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { ActivatedRoute } from '@angular/router';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormsModule, ReactiveFormsModule, FormArray, FormBuilder } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { DragulaModule, DragulaService } from 'ng2-dragula';

import { MoleculesModule } from '../../../../molecules/molecules.module';
import { SharedModule } from '@shared/shared.module';
import { CreateComponent } from '@modules/component/components/create-component/create-component.component';
import { ToolbarComponent } from '@modules/component/components/sub/toolbar/toolbar.component';
import { CardComponent } from '@modules/component/components/sub/card/card.component';
import { StrategyFormComponent } from '@modules/component/components/sub/strategy-form/strategy-form.component';
import { ParametersComponent } from '@modules/component/components/sub/parameters/parameters.component';
import { ChildPanelComponent } from '@modules/component/components/sub/child-panel/child-panel.component';
import { ExecutionPanelComponent } from '@modules/component/components/sub/execution-panel/execution-panel.component';
import { ActionEditComponent } from '@modules/component/components/action/action-edit.component';
import { StrategyParameterFormComponent } from '@modules/component/components/sub/strategy-form/parameter-form/strategy-parameter-form.component';
import { ComponentTask, KeyValue } from '@model';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ActivatedRouteStub } from '../../../../testing/activated-route-stub';
import { ComponentService } from '@core/services';
import { of } from 'rxjs';

describe('CreateComponent...', () => {

    describe('With all child components...', () => {

        let fixture: ComponentFixture<CreateComponent>;
        let component: CreateComponent;

        const dragulaService = new DragulaService();

        beforeEach(async(() => {
            TestBed.resetTestingModule();
            TestBed.configureTestingModule({
                imports: [
                    HttpClientTestingModule,
                    RouterTestingModule,
                    TranslateModule.forRoot(),
                    MoleculesModule,
                    SharedModule,
                    FormsModule,
                    ReactiveFormsModule,
                    DragulaModule,
                ],
                declarations: [
                    CreateComponent,
                    ToolbarComponent,
                    CardComponent,
                    StrategyFormComponent,
                    StrategyParameterFormComponent,
                    ParametersComponent,
                    ChildPanelComponent,
                    ExecutionPanelComponent,
                    ActionEditComponent
                ],
                providers: [
                    {provide: DragulaService, useValue: dragulaService}
                ]
            }).compileComponents();
        }));

        beforeEach(() => {
            fixture = TestBed.createComponent(CreateComponent);
            component = fixture.componentInstance;
            fixture.detectChanges();
        });

        it('should be created', () => {
            expect(component).toBeDefined();
        });
    });

    describe('Ignoring child components...', () => {

        let fixture: ComponentFixture<CreateComponent>;
        let component: CreateComponent;

        const formBuilder = new FormBuilder();
        const dragulaService = new DragulaService();
        const activatedRouteStub = new ActivatedRouteStub();
        const componentService = jasmine.createSpyObj('ComponentService', ['findAllTasks', 'findAllComponent', 'findParents']);

        beforeEach(() => {
            TestBed.resetTestingModule();
            TestBed.configureTestingModule({
                imports: [
                    HttpClientTestingModule,
                    RouterTestingModule.withRoutes([
                        { path: 'component', pathMatch: 'full', redirectTo: 'list' },
                        { path: 'component/:id', component: CreateComponent }
                    ]),
                    TranslateModule.forRoot(),
                    SharedModule,
                ],
                declarations: [
                    CreateComponent,
                ],
                providers: [
                    {provide: ActivatedRoute, useValue: activatedRouteStub},
                    {provide: ComponentService, useValue: componentService},
                    {provide: FormBuilder, useValue: formBuilder},
                    {provide: DragulaService, useValue: dragulaService}
                ],
                schemas: [ NO_ERRORS_SCHEMA ]
            }).compileComponents();

            fixture = TestBed.createComponent(CreateComponent);
            component = fixture.componentInstance;
        });

        describe('Component steps parameters values', () => {

            const oldValue = 'parent value';
            const newValue = 'another parent value';

            const readComponentStepParameterValue = function(stepIndex, parameterIndex, readTime) {
                return new Promise(resolve => {
                    setTimeout(() => resolve(component.componentTasksCreated[stepIndex].dataSet[parameterIndex].value), readTime)
                });
            };

            beforeEach(() => {
                // Given
                const parentComponent = new ComponentTask(
                    'parent component',
                    null,
                    [
                        new ComponentTask('child without parameters', null, [], [], [], [], null, 'child-id-1'),
                        new ComponentTask('child with one parameter', null, [], [new KeyValue('param', 'default value')], [new KeyValue('param', oldValue)], [], null, 'child-id-2')
                    ],
                    [], [], [], null, 'parent-id');

                componentService.findAllTasks.and.returnValue(of([]));
                componentService.findParents.and.returnValue(of({parentSteps: [], parentScenario: []}));
                componentService.findAllComponent.and.returnValue(of([parentComponent]));
                activatedRouteStub.setParamMap({id: parentComponent.id});

                fixture.detectChanges();
            });

            [
                {expectedReadValue: oldValue, waitMs: 240},
                {expectedReadValue: newValue, waitMs: 260}
            ]
                .forEach(data => {
                    it(`should be equals to "${data.expectedReadValue}" after update when read after ${data.waitMs}ms`, () => {
                        // When
                        const componentsParametersValues = component.componentForm.controls.componentsParametersValues as FormArray;
                        (componentsParametersValues.controls[1] as FormArray).controls[0].setValue(newValue);

                        // Then
                        return readComponentStepParameterValue(1, 0, data.waitMs)
                            .then(
                                newReadValue => {
                                    expect(newReadValue).toEqual(data.expectedReadValue);
                                });
                    });
                });
/**
            it('should not be updated before 250ms', () => {
                // When
                const componentsParametersValues = component.componentForm.controls.componentsParametersValues as FormArray;
                (componentsParametersValues.controls[1] as FormArray).controls[0].setValue(newValue);

                // Then
                const readNewvalue = new Promise(resolve => {
                    setTimeout(() => resolve(component.componentTasksCreated[1].dataSet[0].value), 249)
                });
                return readNewvalue
                    .then(
                        newReadValue => {
                            expect(newReadValue).toEqual(oldValue);
                        });
            });

            it('should be updated after 250ms', () => {
                // When
                const componentsParametersValues = component.componentForm.controls.componentsParametersValues as FormArray;
                (componentsParametersValues.controls[1] as FormArray).controls[0].setValue(newValue);

                // Then
                const readNewvalue = new Promise(resolve => {
                    setTimeout(() => resolve(component.componentTasksCreated[1].dataSet[0].value), 250)
                });
                return readNewvalue
                    .then(
                        newReadValue => {
                            expect(newReadValue).toEqual(newValue);
                        });
            });
**/
            it('should be updated after delete a precedent step', () => {
                // When
                component.removeStep(0);
                const componentsParametersValues = component.componentForm.controls.componentsParametersValues as FormArray;
                (componentsParametersValues.controls[0] as FormArray).controls[0].setValue(newValue);

                // Then
                return readComponentStepParameterValue(0, 0, 260)
                    .then(
                        newReadValue => {
                            expect(newReadValue).toEqual(newValue);
                        });
            });
        });
    });
});
