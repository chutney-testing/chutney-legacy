import { AfterViewChecked, Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ExampleParameter, FunctionalStep, GwtTestCase } from '@model';
import { ScenarioService } from '@core/services';
import { BehaviorSubject, Subscription } from 'rxjs';
import { CanDeactivatePage } from '@core/guards';
import { EventManagerService } from '@shared';
import { allStepsParamsFromFunctionStep } from '@shared/tools/function-step-utils';
import { FormArray, FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { JiraLinkService } from '@core/services/jira-link.service';

@Component({
    selector: 'chutney-edition',
    templateUrl: './edition.component.html',
    styleUrls: ['./edition.component.scss']
})
export class EditionComponent extends CanDeactivatePage implements OnInit, OnDestroy, AfterViewChecked {

    previousGwtTestCase: GwtTestCase;
    gwtTestCase: GwtTestCase;
    modificationsSaved = false;
    exampleParams$: BehaviorSubject<any> = new BehaviorSubject<any>({});
    errorMessage: any;

    collapseParam = true;
    collapseTechnicalSteps = true;
    exampleStepIdToScrollTo: string;

    componentForm: FormGroup;
    pluginsForm: FormGroup;

    private routeParamsSubscription: Subscription;

    constructor(private eventManager: EventManagerService,
                private formBuilder: FormBuilder,
                private jiraLinkService: JiraLinkService,
                private route: ActivatedRoute,
                private router: Router,
                private scenarioService: ScenarioService,
    ) {
        super();
        this.gwtTestCase = new GwtTestCase();
        this.previousGwtTestCase = this.gwtTestCase.clone();
        this.componentForm = this.formBuilder.group({
            parameters: this.formBuilder.array([])
        });
        this.pluginsForm = this.formBuilder.group({
            jiraId: ''
        });
    }

    ngOnInit() {
        this.routeParamsSubscription = this.route.params.subscribe((params) => {
            this.load(params['id']);
        });
    }

    ngOnDestroy() {
        this.exampleParams$.unsubscribe();
        this.eventManager.destroy(this.routeParamsSubscription);
    }

    ngAfterViewChecked() {
        if (this.exampleStepIdToScrollTo) {
            const elem = document.getElementById(this.exampleStepIdToScrollTo);
            if (elem) {
                elem.scrollIntoView({behavior: 'smooth', block: 'center', inline: 'center'});
            }
            this.exampleStepIdToScrollTo = null;
        }
    }

    canDeactivatePage(): boolean {
        return this.modificationsSaved || this.gwtTestCase.equals(this.previousGwtTestCase);
    }

    load(id) {
        if (id !== undefined) {
            this.scenarioService.findTestCase(id).subscribe(
                (response) => {
                    this.gwtTestCase = response;
                    this.collapseParam = this.gwtTestCase.wrappedParams.params.length > 0;
                    this.exampleParams$.next(this.gwtTestCase.wrappedParams.serialize());
                    this.previousGwtTestCase = this.gwtTestCase.clone();

                    this.initFormParameters();
                },
                (error) => {
                    console.log(error);
                    this.errorMessage = error._body;
                    this.gwtTestCase = new GwtTestCase(id);
                    this.previousGwtTestCase = this.gwtTestCase.clone();
                }
            );
            this.loadJiraLink(id);
        }
    }

    private loadJiraLink(id: string) {
        this.jiraLinkService.findByScenarioId(id).subscribe(
            (jiraId) => {
                this.pluginsForm.controls['jiraId'].setValue(jiraId);
            },
            (error) => {
                console.log(error);
            }
        );
    }

    saveExample() {
        this.updateScenarioParameters();
        const jiraId = this.pluginsForm.value['jiraId'];
        this.scenarioService.createOrUpdateGwtTestCase(this.gwtTestCase).subscribe(
            (response) => {
                this.modificationsSaved = true;
                this.jiraLinkService.saveForScenario(response, jiraId).subscribe(
                    () => {
                    },
                    (error) => {
                        console.log(error);
                    }
                );
                this.router.navigateByUrl('/scenario/' + response + '/execution/last');
            },
            (error) => {
                console.log(error);
                this.errorMessage = error._body;
            }
        );
    }

    updateTags(event: string) {
        if (event != null && event.length > 0) {
            this.gwtTestCase.tags = event.split(',');
        }
    }

    addGivenStep(givenStepIdx) {
        this.exampleStepIdToScrollTo = 'given-step-' + (givenStepIdx + 1);
        this.gwtTestCase.scenario.givens.splice(givenStepIdx + 1, 0, new FunctionalStep('', ''));
    }

    setGivenStep(givenIdx, givenStep: FunctionalStep) {
        this.gwtTestCase.scenario.givens[givenIdx] = givenStep;
    }

    addThenStep(thenStepIdx) {
        this.exampleStepIdToScrollTo = 'then-step-' + (thenStepIdx + 1);
        this.gwtTestCase.scenario.thens.splice(thenStepIdx + 1, 0, new FunctionalStep('', ''));
    }

    setThenStep(thenIdx, thenStep: FunctionalStep) {
        this.gwtTestCase.scenario.thens[thenIdx] = thenStep;
    }

    deleteGivenStep(index: number) {
        const splicedGivenStep = this.gwtTestCase.scenario.givens.splice(index, 1)[0];
        this.handleStepParamsEvent({'created': [], 'deleted': allStepsParamsFromFunctionStep(splicedGivenStep)});
    }

    deleteThenStep(index: number) {
        if (index > 0) {
            const splicedThenStep = this.gwtTestCase.scenario.thens.splice(index, 1)[0];
            this.handleStepParamsEvent({'created': [], 'deleted': allStepsParamsFromFunctionStep(splicedThenStep)});
        }
    }

    setWhenStep(whenStep: FunctionalStep) {
        this.gwtTestCase.scenario.when = whenStep;
    }

    stepWording(stepType: string, index: number) {
        if (index > 0) {
            return 'And';
        } else {
            return stepType;
        }
    }

    paramValueChange() {
        this.exampleParams$.next(this.serializeFormParameters());
    }

    handleStepParamsEvent(event) {
        let paramToDelete: Array<string> = [];

        if (event.deleted) {
            paramToDelete = event.deleted.map(deletedParamName => {

                if (this.allContainsStepParam(this.gwtTestCase.scenario.givens, deletedParamName) ||
                    this.containsStepParam(this.gwtTestCase.scenario.when, deletedParamName) ||
                    this.allContainsStepParam(this.gwtTestCase.scenario.thens, deletedParamName)) {
                    return '';
                }

                return deletedParamName;
            }).filter(name => name.length > 0);
        }

        if (this.updateFormParameters(event.created, paramToDelete)) {
            this.paramValueChange();
        }
    }

    allContainsStepParam(functionalSteps: Array<FunctionalStep>, deletedParamName: string): boolean {
        for (const functionalStep of functionalSteps) {
            if (this.containsStepParam(functionalStep, deletedParamName)) {
                return true;
            }
        }

        return false;
    }

    containsStepParam(functionalStep: FunctionalStep, deletedParamName: string): boolean {
        if (functionalStep.containsStepParam(deletedParamName)) {
            return true;
        }

        for (const subStep of functionalStep.subSteps) {
            if (this.containsStepParam(subStep, deletedParamName)) {
                return true;
            }
        }

        return false;
    }

    switchCollapseParam() {
        this.collapseParam = !this.collapseParam;
    }

    switchCollapseTechnicalSteps() {
        this.collapseTechnicalSteps = !this.collapseTechnicalSteps;
    }

    private initFormParameters() {
        const parameters = this.componentForm.controls.parameters as FormArray;
        this.gwtTestCase.wrappedParams.params.forEach((keyValue) => {
            parameters.push(
                this.formBuilder.group({
                    key: new FormControl({value: keyValue.name, disabled: true}),
                    value: keyValue.value,
                })
            );
        });
    }

    private updateFormParameters(toCreate: Array<string>, toDelete: Array<string>): boolean {
        let changed = false;
        const parameters = this.componentForm.controls.parameters as FormArray;

        for (let i = 0; i < parameters.length; i++) {
            const parameter = parameters.get(i.toString()) as FormGroup;
            const paramName = parameter.get('key').value;
            const toDeleteIdx = toDelete.indexOf(paramName);
            if (toDeleteIdx > -1) {
                toDelete.splice(toDeleteIdx, 1);
                parameters.removeAt(i);
                changed = true;
            }
            const toCreateIdx = toCreate.indexOf(paramName);
            if (toCreateIdx > -1) {
                toCreate.splice(toCreateIdx, 1);
            }
        }

        toCreate.forEach(param => {
            changed = true;
            parameters.push(
                this.formBuilder.group({
                    key: new FormControl({value: param, disabled: true}),
                    value: '',
                })
            );
        });

        return changed;
    }

    private updateScenarioParameters() {
        this.gwtTestCase.wrappedParams.params = [];
        const parameters = this.componentForm.controls.parameters as FormArray;
        for (let i = 0; i < parameters.length; i++) {
            const parameter = parameters.get(i.toString()) as FormGroup;
            if (parameter.get('key').value !== '') {
                this.gwtTestCase.wrappedParams.params.push(new ExampleParameter(parameter.get('key').value, parameter.get('value').value));
            }
        }
    }

    private serializeFormParameters(): Object {
        const paramJsonObject = {};
        const parameters = this.componentForm.controls.parameters as FormArray;
        for (let i = 0; i < parameters.length; i++) {
            const parameter = parameters.get(i.toString()) as FormGroup;
            paramJsonObject[parameter.get('key').value] = (parameter.get('value').value);
        }
        return paramJsonObject;
    }
}
