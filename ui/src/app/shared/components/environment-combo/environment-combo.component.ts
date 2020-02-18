import { Component, Input, Output, EventEmitter, OnChanges } from '@angular/core';
import { EnvironmentMetadata } from '@model';

@Component({
    selector: 'chutney-environment-combo',
    templateUrl: './environment-combo.component.html',
    styleUrls: ['./environment-combo.component.scss']
})
export class EnvironmentComboComponent implements OnChanges {


    @Input() environments: EnvironmentMetadata[];
    @Input() defaultValue: string;
    @Output() selectionEvent = new EventEmitter();

    selectedEnvironment: EnvironmentMetadata;

    constructor() {
    }

    ngOnChanges() {
        this.setSelectedEnvironment();
    }

    changingValue(event: any) {
        this.selectedEnvironment = this.environments.filter(env => env.name === event.target.value)[0];
        this.selectionEvent.emit(this.selectedEnvironment);
    }

    private setSelectedEnvironment() {
        if (this.environments && this.environments.length > 0) {
            const envFound = this.environments.find(e => e.name === this.defaultValue);
            if (envFound) {
                this.selectedEnvironment = envFound;
            } else {
                this.selectedEnvironment = this.environments[0];
            }
            this.selectionEvent.emit(this.selectedEnvironment);
        }
    }
}
