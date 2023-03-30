import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { v4 as uuidv4 } from 'uuid';
import { FormControl } from '@angular/forms';

@Component({
    selector: 'chutney-import-button',
    templateUrl: './import-button.component.html',
    styleUrls: ['./import-button.component.scss']
})
export class ImportButtonComponent implements OnInit {

    @Input() acceptedTypes = '(text/plain)|(application/json)';
    @Output() submitEvent = new EventEmitter();
    @Input() label: string = 'global.actions.import';
    @Input() btnSizeClass: 'lg' | 'sm';
    @Input() btnColor: 'primary' | 'success' | 'info' | 'warning' | 'danger' = 'success';

    uuid: string;

    fileControl = new FormControl('');

    constructor() {
        this.uuid = uuidv4();
    }

    ngOnInit() {

    }

    handleFileSelection(e) {
        e.stopPropagation();
        e.preventDefault();

        const selectedFile = e.target.files[0];
        if (selectedFile) {
            this.import(selectedFile);
        }
        this.fileControl.reset();
    }

    private import(selectedFile: File) {
        this.submitEvent.emit(selectedFile);
    }

}
