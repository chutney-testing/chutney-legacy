import { Component, EventEmitter, Input, OnChanges, OnInit, Output, ViewChild } from '@angular/core';
import { timer } from 'rxjs';

import 'brace';
import 'brace/ext/searchbox';
import 'brace/mode/json';
import 'brace/mode/hjson';
import 'brace/mode/yaml';
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
export class AceEditorComponent implements OnInit, OnChanges {

    @Input() initialContent: string;
    @Input() options: string;
    @Input() hasError: boolean;
    @Input() modes: Array<string> = ['yaml']
    @Input() showConfiguration = true;
    @Output() textChangeEvent = new EventEmitter();
    @Output() editorBlur = new EventEmitter();
    @Output() editorFocus = new EventEmitter();

    @ViewChild('editor') editor;

    currentMode;

    themes: Array<string> = ['monokai', 'eclipse', 'merbivore'];
    currentTheme: string = this.themes[0];

    constructor() {

    }

    ngOnInit() {
        this.editor.getEditor().on('blur', () => {
            this.editorBlur.emit(event);
        });
        this.editor.getEditor().on('focus', () => {
            this.editorFocus.emit(event);
        });

        this.currentMode = this.modes[0];
        this.editor.mode = this.currentMode;
        this.editor.theme = this.currentTheme;
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
            timer(100).subscribe(() => {
                this.editor.getEditor().clearSelection();
                this.editor.getEditor().focus();
                this.editor.getEditor().moveCursorTo(0, 0);
            });
        }
    }

    changingMode(mode: string) {
        this.currentMode = mode;
        this.editor.mode = this.currentMode;
    }

    changingTheme(theme: string) {
        this.currentTheme = theme;
        this.editor.theme = this.currentTheme;
    }

    onContentChange(event: any) {
        this.textChangeEvent.emit(event);
    }

    forceContentChange(newContent: string) {
        this.initialContent = newContent;
        this.editor.value = newContent;
    }

}

