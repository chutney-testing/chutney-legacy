import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';

@Component({
    selector: 'chutney-import-button',
    templateUrl: './import-button.component.html',
    styleUrls: ['./import-button.component.scss']
})
export class ImportButtonComponent implements OnInit {

    @Input() acceptedTypes = '(text/plain)|(application/json)';
    @Output() submitEvent = new EventEmitter();

    selectedFile: File;

    constructor() {}

    ngOnInit() {
        document.getElementById('input-file-browser')
            .addEventListener('change', (e) => this.handleFileSelection(e), false);
    }

    private handleFileSelection(e) {
        e.stopPropagation();
        e.preventDefault();

        let files = [];
        if (e.dataTransfer != null) {
            files = Array.from(e.dataTransfer.files);
        } else if (e.target != null) {
            files = Array.from(e.target.files);
        }

        this.selectFile(files);
        this.import();
    }

    private selectFile(files: Array<File>) {
        this.selectedFile = files[0];
    }

    import() {
        this.submitEvent.emit(this.selectedFile);
    }

}
