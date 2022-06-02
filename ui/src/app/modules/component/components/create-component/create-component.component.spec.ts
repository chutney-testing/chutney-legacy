import { TestBed, ComponentFixture } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { RouterTestingModule } from '@angular/router/testing';
import { ActivatedRoute } from '@angular/router';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormsModule, ReactiveFormsModule, FormBuilder } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { DragulaModule, DragulaService } from 'ng2-dragula';

import { ActivatedRouteStub } from '../../../../testing/activated-route-stub';

import { MoleculesModule } from '../../../../molecules/molecules.module';
import { SharedModule } from '@shared/shared.module';
import { CreateComponent } from '@modules/component/components/create-component/create-component.component';
import { ToolbarComponent } from '@modules/component/components/sub/toolbar/toolbar.component';
import { StrategyFormComponent } from '@modules/component/components/sub/strategy-form/strategy-form.component';
import { ParametersComponent } from '@modules/component/components/sub/parameters/parameters.component';
import { ChildPanelComponent } from '@modules/component/components/sub/child-panel/child-panel.component';
import { ExecutionPanelComponent } from '@modules/component/components/sub/execution-panel/execution-panel.component';
import { ActionEditComponent } from '@modules/component/components/action/action-edit.component';
import { StrategyParameterFormComponent } from '@modules/component/components/sub/strategy-form/parameter-form/strategy-parameter-form.component';
import { ComponentService } from '@core/services';

describe('CreateComponent...', () => {

    describe('With all child components...', () => {

        let fixture: ComponentFixture<CreateComponent>;
        let component: CreateComponent;

        const dragulaService = new DragulaService();

        beforeEach(() => {
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
        });

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
/** For example
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
*/
        it('should be created', () => {
            fixture.detectChanges();
            expect(component).toBeDefined();
        });
    });
});
