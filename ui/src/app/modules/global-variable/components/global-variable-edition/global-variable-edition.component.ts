import { Component, ViewChild, OnInit } from '@angular/core';
import { AceEditorDirective } from '@shared/ace-editor/ace-editor.directive';
import { GlobalVariableService } from '@core/services/global-var.service';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
    selector: 'chutney-global-variable-edition',
    templateUrl: './global-variable-edition.component.html',
    styleUrls: ['./global-variable-edition.component.scss']
})
export class GlobalVariableEditionComponent implements OnInit {

    editorTheme: EditorTheme = new EditorTheme('Monokai', 'monokai');
    editorMode: EditorMode = new EditorMode('Hjson', 'hjson');

    aceOptions: any = {
        fontSize: '13pt',
        enableBasicAutocompletion: true,
        showPrintMargin: false
    };

    data = '';
    fileNames;
    currentFileName;
    message: string;

    help = false;

    @ViewChild(AceEditorDirective) aceEditorDirective: AceEditorDirective;

    constructor(private globalVariableService: GlobalVariableService) {
    }

    ngOnInit(): void {
        this.globalVariableService.list().subscribe(
            response => {
                this.fileNames = response;
                this.currentFileName = this.fileNames[0];
                this.updateFileContent(this.currentFileName);
            }
        );
    }

    callBackFunc(data) {
        this.data = data;
    }

    save() {
        (async () => {
            this.message = 'Saving...';
            await this.delay(1000);
            this.globalVariableService.save(this.currentFileName, this.data).subscribe(
                res => {
                    this.message = 'Document saved';
                    if (this.fileNames.indexOf(this.currentFileName) === -1) {
                        this.fileNames.push(this.currentFileName);
                    }
                },
                error => this.handleError(error));
        })();
    }

    resizeEditor() {

        const mainContentClientHeight = document.getElementsByClassName('main-content')[0].clientHeight;

        const editorHeight = mainContentClientHeight - 100;

        const editor = document.getElementById('editor');
        if (editor) {
            editor.style.height = editorHeight + 'px';
        }
        if (this.aceEditorDirective) {
            this.aceEditorDirective.editor.resize();
        }
    }

    delay(ms: number) {
        return new Promise(resolve => setTimeout(resolve, ms));
    }

    private handleError(err: HttpErrorResponse) {
        if (err.error instanceof ProgressEvent) {
            this.message = 'Back-end server not reachable';
        } else {
            this.message = err.error;
        }
    }

    updateFileContent(selectedFileName: string) {
        if (selectedFileName === undefined) {
            this.data = '';
            this.aceEditorDirective.editor.setValue('');
        } else {
            this.globalVariableService.get(selectedFileName).subscribe(
                response => {
                    this.data = response;
                    this.aceEditorDirective.editor.setValue(response);
                }
            );
        }
    }

    deleteFile() {
        console.log("bou");
        (async () => {
            this.message = 'Deleting...';
            await this.delay(1000);
            this.globalVariableService.delete(this.currentFileName).subscribe(
                res => {
                    this.fileNames.splice(this.fileNames.indexOf(this.currentFileName), 1);
                    this.currentFileName = this.fileNames[0];
                    this.message = 'Document deleted';
                    this.updateFileContent(this.currentFileName);
                },
                error => this.handleError(error));
        })();
    }
}

class EditorMode {
    constructor(public label: string, public name: string) {
    }
}

class EditorTheme {
    constructor(public label: string, public name: string) {
    }
}
