import {
    Component,
    OnInit,
    Input,
    Output,
    EventEmitter,
    ViewChild,
    ComponentFactoryResolver,
    AfterViewChecked,
    ElementRef,
    ViewEncapsulation,
    OnDestroy,
    AfterViewInit,
    OnChanges,
    SimpleChanges
} from '@angular/core';
import { Subject, BehaviorSubject, fromEvent, timer } from 'rxjs';
import { debounceTime, distinctUntilChanged, map } from 'rxjs/internal/operators';
import { FunctionalStep, TechnicalStep } from '@model';
import { TechnicalStepComponent } from '@shared/components/technical-step/technical-step.component';
import { ImplementationHostDirective } from '@shared/directives';
import { distinct } from '@shared/tools';
import {
    allStepsParamsFromFunctionStep,
    stepsParamsFromFunctionStep,
    highlightStepParams,
    exampleParamsExistStepParams,
    highlightUnknownParams
} from '@shared/tools/function-step-utils';

@Component({
    selector: 'chutney-functional-step',
    templateUrl: './functional-step.component.html',
    styleUrls: ['./functional-step.component.scss'],
    encapsulation: ViewEncapsulation.None
})
export class FunctionalStepComponent implements OnInit, OnChanges, OnDestroy, AfterViewInit, AfterViewChecked {

    @Input() step: FunctionalStep;
    @Input() id: string;
    @Input() stepWording: string;
    @Input() hideDelete = false;
    @Input() hideAnd: boolean;
    @Input() placeholder: string;
    @Input() ancestryIndex = 0;
    @Input() exampleStepsType: number;
    @Input() exampleParams$: BehaviorSubject<any>;
    @Input() showTechnicalStep: boolean = false;

    @Output() deleteEvent = new EventEmitter();
    @Output() andEvent = new EventEmitter();
    @Output() refStepEvent = new EventEmitter();
    @Output() stepParamsEvent = new EventEmitter();

    stepTextValueObs$ = new Subject<string>();

    stepDescriptionEdition: boolean = false;
    stepImplementationEdition: boolean = false;
    subStepIdToScrollTo: string;

    private stepParams: Array<string> = [];

    private scrollTo: boolean = false;

    @ViewChild(ImplementationHostDirective) implementationHost: ImplementationHostDirective;
    @ViewChild('preStepDescription') preStepDescription: ElementRef;

    constructor(private componentFactoryResolver: ComponentFactoryResolver) {
    }

    ngOnInit() {
        this.stepTextValueObs$.pipe(
            map(value => value.trim()),
            debounceTime(1000),
            distinctUntilChanged()
        ).subscribe(
            (value: string) => this.evaluateTextForParams()
        );
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (this.step.implementation != null && this.step.subSteps.length === 0) {
            if (this.showTechnicalStep) {
                this.showImplementation();
            } else {
                this.hideImplementation();
            }
        }
    }

    ngOnDestroy() {
        this.stepTextValueObs$.complete();
    };

    ngAfterViewInit() {
        this.exampleParams$.pipe(
            distinctUntilChanged()
        ).subscribe(
            (params: any) => {
                this.checkStepPreDescription(params);
            },
            (err) => console.log(err)
        );
        this.evaluateTextForParams(false);
    };

    ngAfterViewChecked() {
        if (this.subStepIdToScrollTo) {
            const elem = document.getElementById(this.subStepIdToScrollTo);
            if (elem) {
                elem.scrollIntoView({behavior: 'smooth', block: 'center', inline: 'center'});
            }
            this.subStepIdToScrollTo = null;
        }

        if (this.stepDescriptionEdition) {
            const editableElement: HTMLElement = document.getElementById(this.id + '-edit');
            fromEvent(editableElement, 'change').pipe(
                debounceTime(1500)
            ).subscribe((event) => this.checkStepPreDescription(this.exampleParams$.getValue()));

            editableElement.focus();
        } else {
            this.checkStepPreDescription(this.exampleParams$.getValue())
        }

        if (this.scrollTo) {
            const elem = document.getElementById(this.id + 'scroll-to');
            if (elem) {
                setTimeout(() => elem.scrollIntoView({behavior: 'smooth', block: 'center', inline: 'center'}), 200);
                this.scrollTo = false;
            }
        }
    }

    addSubStep(subStepIdx) {
        this.subStepIdToScrollTo = this.id + '-' + (subStepIdx + 1);
        this.step.subSteps.splice(subStepIdx + 1, 0, new FunctionalStep('', ''));
    }

    setSubStep(subStepIdx, step: FunctionalStep) {
        this.step.subSteps[subStepIdx] = step;
    }

    deleteStep() {
        this.deleteEvent.emit();
    }

    and() {
        this.andEvent.emit();
    }

    deleteSubStep(index: number) {
        const splicedSubStep = this.step.subSteps.splice(index, 1)[0];

        const deletedParams = [];
        allStepsParamsFromFunctionStep(splicedSubStep)
            .forEach(stepParam => deletedParams.push(stepParam));
        if (deletedParams.length > 0) {
            this.stepParamsEvent.emit({'created': [], 'deleted': deletedParams});
        }
    }

    deleteImplementation() {
        this.step.implementation = null;
        this.hideImplementation();
        this.evaluateTextForParams();
    }

    clickCode(event) {
        event.stopPropagation();
        if (this.step.implementation == null && this.step.subSteps.length === 0) {
            this.addImplementation();
        } else {
            if (this.implementationHost.viewContainerRef.length === 0) {
                this.showImplementation();
            } else {
                this.hideImplementation();
            }
        }
    }

    addImplementation() {
        this.step.implementation = new TechnicalStep('');
        this.showImplementation();
    }

    showImplementation() {
        const componentFactory = this.componentFactoryResolver.resolveComponentFactory(TechnicalStepComponent);
        const viewContainerRef = this.implementationHost.viewContainerRef;
        viewContainerRef.clear();
        const componentRef: TechnicalStepComponent = viewContainerRef.createComponent(componentFactory).instance;
        componentRef.id = this.id + '-tech';
        componentRef.step = this.step.implementation;
        componentRef.exampleParams$ = this.exampleParams$;
        componentRef.stepParams = this.stepParams;
        componentRef.deleteEvent.subscribe(() => this.deleteImplementation());
        componentRef.editionEvent.subscribe((edition: boolean) => this.stepImplementationEdition = edition);

        componentRef.stepParamsEvent.pipe(
            debounceTime(500)
        ).subscribe((event) => this.evaluateTextForParams());

        this.showTechnicalStep = true;
    }

    hideImplementation() {
        this.implementationHost.viewContainerRef.clear();
        this.showTechnicalStep = false;
    }

    descriptionKeyUp(event) {
        if ((event.altKey || event.keyCode === 18) || (event.ctrlKey || event.keyCode === 17) || event.keyCode === 9) {
            return;
        }
        this.stepTextValueObs$.next(event.target.value);
    }

    descriptionBlur() {
        timer(250).subscribe(() => {
            this.stepDescriptionEdition = false;
        });
    }

    descriptionFocus(event?) {
        if (event) {
            event.stopPropagation();
        }

        timer(250).subscribe(() => {
            this.stepDescriptionEdition = true;
            this.scrollTo = true;
        });
    }

    checkStepPreDescription(exampleParamsSerialized: any) {
        if (this.preStepDescription) {
            if (this.stepParams.length > 0 && exampleParamsSerialized != null && exampleParamsExistStepParams(exampleParamsSerialized, this.stepParams)) {
                this.preStepDescription.nativeElement.innerHTML = highlightUnknownParams(highlightStepParams(this.step.sentence, exampleParamsSerialized));
            } else {
                this.preStepDescription.nativeElement.innerHTML = highlightUnknownParams(this.step.sentence);
            }
        }
    }

    descriptionNbLines(): number {
        return this.step.sentence.split('\n').length;
    }

    evaluateTextForParams(emitEvent: boolean = true) {
        const deletedParams: Array<string> = [];
        const createdParams: Array<string> = [];

        // Get stepParams from text
        let matches = stepsParamsFromFunctionStep(this.step);
        matches = distinct(matches);
        if (matches) {
            if (emitEvent) {
                // Compare with current stepParams for creation
                matches.forEach(stepParam => {
                    if (this.stepParams.indexOf(stepParam) === -1) {
                        createdParams.push(stepParam);
                    }
                });

                // Compare with current stepParams for deletion
                this.stepParams.forEach(stepParam => {
                    if (matches.indexOf(stepParam) === -1) {
                        deletedParams.push(stepParam);
                    }
                });
            }

            this.stepParams.length = 0;
            matches.forEach(stepParam => this.stepParams.push(stepParam));
        } else {
            this.stepParams.forEach(stepParam => deletedParams.push(stepParam));
            this.stepParams.length = 0;
        }

        if (createdParams.length > 0 || deletedParams.length > 0) {
            this.stepParamsEvent.emit({'created': createdParams, 'deleted': deletedParams});
        }
    }

    propagateStepParamsEvent(event) {
        this.stepParamsEvent.emit(event);
    }
}
