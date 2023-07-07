import { Component, EventEmitter, Input, Output } from '@angular/core';

import { Entry } from '@model';
import { ValidationService } from '../../validation/validation.service';

@Component({
    selector: 'chutney-property-table-panel',
    templateUrl: './property-table-panel.component.html',
    styleUrls: ['./property-table-panel.component.scss']
})
export class PropertyTablePanelComponent {

    @Input() entries: Entry[];
    @Output() entriesChange: EventEmitter<Entry[]> = new EventEmitter<Entry[]>();


    constructor(public validationService: ValidationService) { }

    createEntry() {
        if (!this.entries) {
            this.entries = [];
        }
        this.entries.unshift(new Entry('', ''));
        this.entriesChange.emit(this.entries)
    }

    deleteEntry(entry: Entry) {
        this.entries.splice(this.entries.indexOf(entry), 1);
        this.entriesChange.emit(this.entries)
    }
}
