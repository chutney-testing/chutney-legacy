import {Component, OnInit} from '@angular/core';
import {GlobalVariableService} from '@core/services/global-var.service';
import {HttpErrorResponse} from '@angular/common/http';

import { Authorization } from '@model';

@Component({
    selector: 'chutney-global-variable-edition',
    templateUrl: './global-variable-edition.component.html',
    styleUrls: ['./global-variable-edition.component.scss']
})
export class GlobalVariableEditionComponent implements OnInit {

    globalVar = '';
    editedGlobalVar = '';
    fileNames;
    currentFileName;
    message: string;

    help = false;

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

    save() {
        (async () => {
            this.globalVar = this.editedGlobalVar;
            this.message = 'Saving...';
            await this.delay(1000);
            this.globalVariableService.save(this.currentFileName, this.globalVar).subscribe(
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
            this.globalVar = '';
        } else {
            this.globalVariableService.get(selectedFileName).subscribe(
                response => {
                    this.globalVar = response;
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

    onContentVarChange(content: string) {
        this.editedGlobalVar = content;
    }
}
