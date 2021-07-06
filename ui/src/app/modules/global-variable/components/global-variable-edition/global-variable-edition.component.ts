import {Component, OnInit, ViewChild} from '@angular/core';
import {GlobalVariableService} from '@core/services/global-var.service';
import {HttpErrorResponse} from '@angular/common/http';

import {AceEditorComponent} from "@shared/ace-editor/ace-editor.component";
import { Authorization } from '@model';

@Component({
    selector: 'chutney-global-variable-edition',
    templateUrl: './global-variable-edition.component.html',
    styleUrls: ['./global-variable-edition.component.scss']
})
export class GlobalVariableEditionComponent implements OnInit {

    data = '';
    fileNames;
    currentFileName;
    message: string;

    help = false;

    @ViewChild('aceEditorGlobalVar') aceEditor: AceEditorComponent;

    Authorization = Authorization;

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
            this.aceEditor.forceContentChange('');
        } else {
            this.globalVariableService.get(selectedFileName).subscribe(
                response => {
                    this.data = response;
                    this.aceEditor.forceContentChange(response);
                }
            );
        }
    }

    deleteFile() {
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
