import { Component, Input, Output, EventEmitter } from '@angular/core';

import {
    ComponentTask
} from '@model';
import { FormGroup } from '@angular/forms';


@Component({
    selector: 'chutney-card-component',
    templateUrl: './card.component.html',
    styleUrls: ['./card.component.scss']
})
export class CardComponent {

    @Input() component: ComponentTask;
    @Input() cardForm: FormGroup;
    @Input() componentIndex: number = 0;

    @Output() deleteEvent = new EventEmitter();

    collapseComponentsParameters = true;

    constructor(
    ) {
    }

    switchCollapseComponentsParameter() {
        this.collapseComponentsParameters = !this.collapseComponentsParameters;
    }

    delete() {
        this.deleteEvent.emit();
    }
}
