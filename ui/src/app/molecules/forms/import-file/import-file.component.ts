import {Component, OnInit, Input, Output, EventEmitter, OnDestroy} from '@angular/core';

@Component({
    selector: 'chutney-import-file',
    templateUrl: './import-file.component.html',
    styleUrls: ['./import-file.component.scss']
})
export class ImportFileComponent implements OnInit {

    @Input() multiple = false;
    @Input() acceptedTypes = '(text/plain)|(application/json)';
    @Output() importEvent = new EventEmitter();

    selectedFiles: Array<File> = new Array<File>();

    constructor() {}

    ngOnInit() {
        const dropbox = document.getElementById('drop-box');
        dropbox.addEventListener('dragstart', (e) => this.dragStart(e), false);
        dropbox.addEventListener('dragleave', (e) => this.dragLeave(e), false);
        dropbox.addEventListener('dragover', (e) => this.dragOver(e), false);
        dropbox.addEventListener('drop', (e) => this.handleFileSelection(e), false);

        document.getElementById('input-file-browser')
                .addEventListener('change', (e) => this.handleFileSelection(e), false);
    }

    private dragStart(e) {
        e.stopPropagation();
        e.preventDefault();
        e.dataTransfer.effectAllowed = 'copy';
    }

    private dragLeave(e) {
        e.stopPropagation();
        e.preventDefault();
        document.getElementById('drop-box').classList.remove('hover');
    }

    private dragOver(e) {
        e.stopPropagation();
        e.preventDefault();
        e.dataTransfer.dropEffect = 'copy';
        document.getElementById('drop-box').classList.add('hover');
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

        if (this.multiple) {
            this.selectFiles(files);
        } else {
            this.selectFile(files);
        }
        document.getElementById('drop-box').classList.remove('hover');
    }

    private selectFile(files: Array<File>) {
        this.selectedFiles[0] = files[0];
    }

    private selectFiles(files: Array<File>): any {
        for (let i = 0; i < files.length; i++) {
            if (this.isNotSelected(files[i]) && this.isAccepted(files[i].type)) {
                this.selectedFiles.push(files[i]);
            }
        }
    }

    private isNotSelected(file: File) {
        const found = this.selectedFiles.filter(f => this.isEqual(f, file));
        return found.length === 0;
    }

    private isEqual(first: File, second: File): boolean {
        return Object.is(first.lastModified, second.lastModified)
            && Object.is(first.name, second.name)
            && Object.is(first.size, second.size)
            && Object.is(first.type, second.type);
    }

    private isAccepted(type: string) {
        return type.match('^' + this.acceptedTypes + '$');
    }

    import() {
        this.importEvent.emit(this.selectedFiles);
    }

    cancel() {
        this.selectedFiles = [];
    }
}
