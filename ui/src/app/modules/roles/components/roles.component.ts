import { Component, OnInit, ViewChild } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

import { RolesService } from '@core/services';
import {AceEditorComponent} from "@shared/ace-editor/ace-editor.component";
import { delay } from '@shared/tools';

@Component({
    selector: 'chutney-roles',
    templateUrl: './roles.component.html',
    styleUrls: ['./roles.component.scss']
})
export class RolesComponent implements OnInit {

    rolesContent: string;
    message: string;
    help: boolean;
    error: boolean;
    @ViewChild('rolesAceEditor') rolesAceEditor: AceEditorComponent;

    private saving: string;
    private saved: string;

    constructor(
        private rolesService: RolesService,
        private translate: TranslateService,
    ) {
        translate.get('global.actions.ongoing.saving').subscribe((res: string) => {
            this.saving = res;
        });
        translate.get('global.actions.done.saved').subscribe((res: string) => {
            this.saved = res;
        });
    }

    ngOnInit() {
        this.loadRoles();
    }

    saveRoles() {
        try {
            const content = JSON.parse(this.rolesAceEditor.editor.value);
            (async () => {
                this.printMessage(this.saving);
                await delay(1000);
                this.rolesService.save(content).subscribe(
                    res => {
                        this.printMessage(this.saved);
                        this.loadRoles();
                    },
                    err => {
                        this.printMessage((err.error || `${err.status} ${err.statusText}`), true);
                    }
                );
            })();
        } catch(e) {
            this.printMessage(e, true);
        }
    }

    private loadRoles() {
        this.rolesService.read().subscribe(
            (res) => {
                this.rolesContent = JSON.stringify(res, undefined, '\t');
                this.rolesAceEditor && this.rolesAceEditor.forceContentChange(this.rolesContent);
            },
            (err) => {
                this.printMessage(err.error || `${err.status} ${err.statusText}`, true);
            }
        );
    }

    private printMessage(message: string, err: boolean = false) {
        this.error = err;
        this.message = message;
    }
}
