import {
    Component,
    Input,
    Output,
    EventEmitter,
    ViewChild,
    ElementRef,
    ViewEncapsulation,
    AfterViewInit,
    AfterViewChecked
} from '@angular/core';
import { TechnicalStep } from '@model';
import { BehaviorSubject, timer } from 'rxjs/index';
import {
    exampleParamsExistStepParams,
    highlightStepParams,
    highlightUnknownParams
} from '@shared/tools/function-step-utils';
import { distinctUntilChanged } from 'rxjs/operators';

@Component({
    selector: 'chutney-technical-step',
    templateUrl: './technical-step.component.html',
    styleUrls: ['./technical-step.component.scss'],
    encapsulation: ViewEncapsulation.None
})
export class TechnicalStepComponent implements AfterViewInit, AfterViewChecked {

    @Input() step: TechnicalStep;
    @Input() id: string;
    @Input() exampleParams$: BehaviorSubject<any>;
    @Input() stepParams: Array<string> = [];

    @Output() deleteEvent = new EventEmitter();
    @Output() stepParamsEvent = new EventEmitter();
    @Output() editionEvent = new EventEmitter();

    implementationEdition: boolean = false;
    placeholder: string = 'Add implementation';

    private scrollTo: boolean = false;

    @ViewChild('preStepImplementation') preStepImplementation: ElementRef;
    private preStepImplementationUpdate: boolean = false;

    constructor() {
    }

    ngAfterViewInit() {
        this.exampleParams$.pipe(
            distinctUntilChanged()
        ).subscribe(
            (params: any) => this.checkPreStepImplementation(params),
            (err) => console.log(err)
        );
    };

    ngAfterViewChecked() {
        if (this.preStepImplementationUpdate) {
            this.preStepImplementationUpdate = false;
            this.checkPreStepImplementation(this.exampleParams$.getValue());
        }
        if (this.scrollTo) {
            const elem = document.getElementById(this.id + 'scroll-to');
            if (elem) {
                setTimeout(() => elem.scrollIntoView({behavior: 'smooth', block: 'center', inline: 'start'}), 200);
                this.scrollTo = false;
            }
        }
    }

    deleteStep() {
        this.step = null;
        this.deleteEvent.emit();
    }

    onScenarioContentChanged(data) {
        this.step.task = data;
        this.stepParamsEvent.emit(data);
    }

    implementationBlur() {
        if (this.step) {
            timer(100).subscribe(() => {
                this.implementationEdition = false;
                this.preStepImplementationUpdate = true;
                this.editionEvent.emit(false);
            });

        }
    }

    implementationFocus() {
        timer(100).subscribe(() => {
            this.implementationEdition = true;
            this.scrollTo = true;
            this.editionEvent.emit(true);
        });
    }

    checkPreStepImplementation(exampleParamsSerialized: any) {
        if (this.preStepImplementation) {
            if (this.stepParams.length > 0 && exampleParamsSerialized != null && exampleParamsExistStepParams(exampleParamsSerialized, this.stepParams)) {
                this.preStepImplementation.nativeElement.innerHTML = highlightUnknownParams(highlightStepParams(this.step.task, exampleParamsSerialized));
            } else {
                this.preStepImplementation.nativeElement.innerHTML = highlightUnknownParams(this.step.task);
            }
        }
    }
}
