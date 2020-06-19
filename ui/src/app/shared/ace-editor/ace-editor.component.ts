import {AfterViewInit, Component, EventEmitter, Input, OnChanges, Output, ViewChild} from '@angular/core';
import { fromEvent } from 'rxjs';

import 'brace';
import 'brace/mode/json';
import 'brace/mode/hjson';
import 'brace/mode/html';
import 'brace/mode/asciidoc';
import 'brace/theme/monokai';
import 'brace/theme/eclipse';
import 'brace/theme/merbivore';

@Component({
    selector: 'chutney-ace-editor',
    templateUrl: './ace-editor.component.html',
    styleUrls: ['./ace-editor.component.scss']
})
export class AceEditorComponent implements AfterViewInit, OnChanges {

    @Input() initialContent: string;
    @Input() options: string;
    @Input() hasError: boolean;
    @Input() modes: Array<string>
    @Input() showConfiguration = true;
    @Output() textChangeEvent = new EventEmitter();
    @Output() editorBlur = new EventEmitter();

    @ViewChild('editor') editor;

    editorModes: Array<string> = ['json', 'hjson'];
    editorMode = this.editorModes[1];

    editorThemes: Array<string> = ['monokai', 'eclipse', 'merbivore'];
    editorTheme: string = this.editorThemes[0];

    constructor() {

    }

    ngAfterViewInit() {
        /* fromEvent(this.editor, 'blur').subscribe(event => {
            console.log(event);
            this.editorBlur.emit(event);
        }); */

        if (this.modes) {
            this.editorModes = this.modes;
        } else {
            this.editor.mode = this.editorMode;
        }
        this.editor.theme = this.editorTheme;
        if (this.options) {
            this.editor.options = this.options;
        } else {
            this.editor.options = {
                fontSize: '13pt',
                showPrintMargin: false
            };
        }
    }

    ngOnChanges(): void {
        if (this.editor.value.length === 0) {
            this.editor.value = this.initialContent;
        }
    }

    changingMode(event: any) {
        this.editorMode = this.editorModes.filter(env => env === event.target.value)[0];
        this.editor.mode = this.editorMode;
    }

    changingTheme(event: any) {
        this.editorTheme = this.editorThemes.filter(env => env === event.target.value)[0];
        this.editor.theme = this.editorTheme;
    }

    onContentChange(event: any) {
        this.textChangeEvent.emit(event);
    }

    forceContentChange(newContent: string) {
        this.initialContent = newContent;
        this.editor.value = newContent;
    }

}

