import { NgModule, Component, ViewChildren, QueryList } from '@angular/core';
import { TestBed, ComponentFixture } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ComponentCardComponent } from '@shared/components';

import { TranslateTestingModule } from '../../../testing/translate-testing.module';
import { HasAuthorizationDirective } from '../../../shared/directives/hasAuthorization.directive';

import { ComponentTask, KeyValue, Implementation, Authorization } from '@model';
import { LoginService } from '@core/services';

describe('ComponentCardComponent...', () => {

    const loginService = jasmine.createSpyObj('LoginService', ['hasAuthorization']);
    loginService.hasAuthorization.and.callFake(
        function(a) {
            return a[0] === Authorization.COMPONENT_WRITE;
        }
    );

    describe('StandAlone...', () => {

        let fixture: ComponentFixture<ComponentCardComponent>;
        let component: ComponentCardComponent;
        let page: Page;

        beforeEach(() => {
            TestBed.resetTestingModule();
            TestBed.configureTestingModule({
                imports: [
                    HttpClientTestingModule,
                    RouterTestingModule,
                    TranslateTestingModule,
                    FormsModule,
                    ReactiveFormsModule,
                    HasAuthorizationTestingModule
                ], declarations: [
                    ComponentCardComponent
                ], providers: [
                    { provide: LoginService, useValue: loginService }
                ]
            }).compileComponents();

            fixture = TestBed.createComponent(ComponentCardComponent);
            component = fixture.componentInstance;
            page = new Page(fixture);

            component.ngOnChanges({}); // Trigger only when @Input valued
        });

        it('should be created', () => {
            fixture.detectChanges();
            expect(component).toBeDefined();
        });

        describe('Render...', () => {

            const simpleComponent = buildComponentTask('Simple component');
            const parametersComponent = buildComponentTask('Component with parameters', null, buildParameters(2));
            const actionComponent = buildComponentTask('Action component', buildImplementation('identifier'));

            it('should show delete button', () => {
                [simpleComponent, actionComponent].forEach(c => {
                    component.component = c;
                    fixture.detectChanges();
                    expect(page.deleteBtn).not.toBeNull();
                });
            });

            it('should show type icone component', () => {
                component.component = actionComponent;
                fixture.detectChanges();

                let componentType = page.type.classList;
                expect(componentType).toContain('fa');
                expect(componentType).toContain('fa-clone');

                component.component = simpleComponent;
                fixture.detectChanges();

                componentType = page.type.classList;
                expect(componentType).toContain('fa');
                expect(componentType).toContain('fa-cubes');
            });

            it('should show component name', () => {
                [simpleComponent, actionComponent, parametersComponent].forEach(c => {
                    component.component = c;
                    fixture.detectChanges();
                    expect(page.name.textContent.trim()).toEqual(c.name);
                });
            });

            it('should show identifer for component with implementation', () => {
                component.component = actionComponent;
                fixture.detectChanges();
                expect(page.identifier.textContent.trim()).toEqual('(' + component.component.implementation.identifier+')');

                component.component = simpleComponent;
                fixture.detectChanges();
                expect(page.identifier).toBeNull();
            });

            it('should show parameters form on collapse button click', () => {
                [simpleComponent, actionComponent].forEach(c => {
                    component.component = c;
                    fixture.detectChanges();
                    expect(page.parameters).toBeNull();
                });

                component.component = parametersComponent;
                fixture.detectChanges();
                expect(page.parametersCollapseBtn).not.toBeNull();
                // Not show by default
                parametersComponent.computedParameters.forEach((kv, index) => {
                    expect(page.parameterKey(index)).toBeUndefined();
                    expect(page.parameterValue(index)).toBeUndefined();
                });
                page.parametersCollapseBtn.click();
                fixture.detectChanges();
                parametersComponent.computedParameters.forEach((kv, index) => {
                    expect(page.parameterKey(index).textContent).toEqual(kv.key);
                    expect(page.parameterValue(index).value).toEqual(kv.value);
                });
                page.parametersCollapseBtn.click();
                fixture.detectChanges();
                parametersComponent.computedParameters.forEach((kv, index) => {
                    expect(page.parameterKey(index)).toBeUndefined();
                    expect(page.parameterValue(index)).toBeUndefined();
                });
            });
        });
    });

    describe('TestHost...', () => {

        let fixture: ComponentFixture<TestHostComponent>;
        let testHostComponent: TestHostComponent;
        let page: HostPage;

        beforeEach(() => {
            TestBed.resetTestingModule();
            TestBed.configureTestingModule({
                imports: [
                    HttpClientTestingModule,
                    RouterTestingModule,
                    TranslateTestingModule,
                    FormsModule,
                    ReactiveFormsModule,
                    HasAuthorizationTestingModule
                ], declarations: [
                    TestHostComponent,
                    ComponentCardComponent
                ], providers: [
                    { provide: LoginService, useValue: loginService }
                ]
            }).compileComponents();

            fixture = TestBed.createComponent(TestHostComponent);
            testHostComponent = fixture.componentInstance;
            page = new HostPage(fixture);
        });

        it('should be created with no components', () => {
            fixture.detectChanges();
            expect(testHostComponent).toBeDefined();
            expect(page.components.length).toEqual(0);
        });

        it('should delete component on delete button click', () => {
            const c1 = buildComponentTask('c1');
            const c2 = buildComponentTask('c2');
            testHostComponent.componentTasks.push(c1, c2);
            fixture.detectChanges();
            expect(page.components.length).toEqual(2);

            page.componentDeleteBtn(0).click();
            fixture.detectChanges();
            expect(page.components.length).toEqual(1);
            expect(testHostComponent.componentTasks.length).toEqual(1);
            expect(testHostComponent.componentTasks[0]).toEqual(c2);
        });

        describe('Parameters values...', () => {

            const paramName = 'param';
            const oldValue = 'parent value';
            const newValue = 'new parent value';

            const readHostComponentParameterValue = function (componentIndex, parameterIndex, readTime) {
                return new Promise(resolve => {
                    setTimeout(() => resolve(testHostComponent.componentTasks[componentIndex].computedParameters[parameterIndex].value),
                                            readTime)
                });
            };

            const simpleComponent: ComponentTask = buildComponentTask('Simple component');
            const parametersComponent: ComponentTask = buildComponentTask('Component with parameters', null, [new KeyValue(paramName, oldValue)]);

            beforeEach(() => {
                // Set inputs and first render
                testHostComponent.componentTasks.push(simpleComponent, parametersComponent);
                fixture.detectChanges();
                // Show all parameters
                testHostComponent.componentCards.toArray().forEach(c => c.collapseComponentsParameters = false);
                fixture.detectChanges();
            });

            [
                {expectedReadValue: oldValue, waitMs: 240},
                {expectedReadValue: newValue, waitMs: 260}
            ]
                .forEach(data => {
                    it(`should be equals to "${data.expectedReadValue}" after update when read after ${data.waitMs}ms`, () => {
                        // When
                        testHostComponent.componentCards.toArray()[1].cardForm.get(paramName).setValue(data.expectedReadValue);

                        // Then
                        return readHostComponentParameterValue(1, 0, data.waitMs)
                            .then(
                                newReadValue => {
                                    expect(newReadValue).toEqual(data.expectedReadValue);
                                });
                    });
                });

            it('should be updated after delete a precedent step', () => {
                // When
                testHostComponent.removeComponent(0);
                fixture.detectChanges();
                testHostComponent.componentCards.toArray()[0].cardForm.get(paramName).setValue(newValue);

                // Then
                return readHostComponentParameterValue(0, 0, 260)
                    .then(
                        newReadValue => {
                            expect(newReadValue).toEqual(newValue);
                        });
            });
        });
    });

});

@Component({
    template: `
        <div *ngFor="let component of componentTasks; let i=index">
            <chutney-component-card
                [component]="component"
                (deleteEvent)="removeComponent(i)">
            </chutney-component-card>
        </div>`
})
class TestHostComponent {
    @ViewChildren(ComponentCardComponent) componentCards: QueryList<ComponentCardComponent>;
    componentTasks: Array<ComponentTask> = [];

    removeComponent(index: number) {
        this.componentTasks.splice(index, 1);
    }
}

class Page {

    private fixture: ComponentFixture<ComponentCardComponent>;

    constructor(fixture: ComponentFixture<ComponentCardComponent>) {
        this.fixture = fixture;
    }

    get root() {
        return this.query<HTMLElement>('.card-body');
    }

    get deleteBtn(): HTMLButtonElement {
        return this.query<HTMLButtonElement>('button');
    }

    get type(): HTMLElement {
        return this.queryAll<HTMLElement>('span')[1];
    }

    get name(): HTMLElement {
        return this.queryAll<HTMLElement>('span')[2];
    }

    get identifier(): HTMLElement {
        return this.query<HTMLElement>('.scenario-components-identifier');
    }

    get parameters(): HTMLElement {
        return this.query<HTMLElement>('.scenario-components-parameters');
    }

    get parametersCollapseBtn(): HTMLButtonElement {
        return this.parameters.querySelector('button');
    }

    public parameterKey(parameterIndex: number): HTMLLabelElement {
        return this.parameters.querySelectorAll('label')[parameterIndex];
    }

    public parameterValue(parameterIndex: number): HTMLInputElement {
        return this.parameters.querySelectorAll('input')[parameterIndex];
    }

    private query<T>(selector: string): T {
        return this.fixture.nativeElement.querySelector(selector);
    }

    private queryAll<T>(selector: string): Array<T> {
        return this.fixture.nativeElement.querySelectorAll(selector);
    }
}

class HostPage {

    private fixture: ComponentFixture<TestHostComponent>;

    constructor(fixture: ComponentFixture<TestHostComponent>) {
        this.fixture = fixture;
    }

    get components(): Array<HTMLElement> {
        return this.queryAll<HTMLElement>('.card-body');
    }

    public component(index: number): HTMLElement {
        return this.components[index];
    }

    public componentDeleteBtn(index: number): HTMLButtonElement {
        return this.component(index).querySelector('button');
    }

    private queryAll<T>(selector: string): T[] {
        return this.fixture.nativeElement.querySelectorAll(selector);
    }
}

function buildComponentTask(name: string,
                            implementation: Implementation = null,
                            parameters: Array<KeyValue> = [],
                            id?: string): ComponentTask {
    return new ComponentTask(name, implementation, [], [], parameters, [], null, id);
}

function buildImplementation(identifier: string): Implementation {
    return new Implementation(identifier, null, false, [], [], [], [], []);
}

function buildParameters(nb: number): Array<KeyValue> {
    const r: Array<KeyValue> = [];
    for (let i = 0; i < nb; i++) {
        r.push(new KeyValue(`param${i}`, `value${i}`));
    }
    return r;
}

@NgModule({
    declarations: [
        HasAuthorizationDirective
    ],
    exports: [
        HasAuthorizationDirective
    ]
})
class HasAuthorizationTestingModule {
}
