import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';

import {
    ComponentTask
} from '@model';
import { EnvironmentService } from '@core/services';

@Component({
    selector: 'chutney-toolbar-component',
    templateUrl: './toolbar.component.html',
    styleUrls: ['./toolbar.component.scss']
})
export class ToolbarComponent implements OnInit{

    @Input() editComponent: ComponentTask;
    @Input() parents: any;

    @Output() editEvent = new EventEmitter();
    @Output() cancelEvent = new EventEmitter();
    @Output() executeEvent = new EventEmitter();
    @Output() childEvent = new EventEmitter();
    @Output() deleteEvent = new EventEmitter();
    @Output() duplicateEvent = new EventEmitter();

    environments: Array<string>;

    constructor(
        private environmentService: EnvironmentService,
    ) {
    }

    ngOnInit(): void {
        this.environmentService.names().subscribe(
            (res) => this.environments = res
        );
    }


    edit() {
        this.editEvent.emit();
    }

    cancel() {
        this.cancelEvent.emit();
    }

    execute(envName: string) {
        this.executeEvent.emit(envName);
    }

    seeChild() {
        this.childEvent.emit();
    }

    delete() {
        this.deleteEvent.emit();
    }

    duplicate() {
        this.duplicateEvent.emit();
    }
}
