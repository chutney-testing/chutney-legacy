import {
    AfterViewChecked,
    AfterViewInit,
    Component,
    EventEmitter,
    Input,
    OnChanges, OnInit,
    Output,
    ViewChild
} from '@angular/core';
import {fromEvent, timer} from 'rxjs';

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
export class AceEditorComponent implements OnInit, OnChanges {

    @Input() initialContent: string;
    @Input() options: string;
    @Input() hasError: boolean;
    @Input() modes: Array<string>
    @Input() showConfiguration = true;
    @Output() textChangeEvent = new EventEmitter();
    @Output() editorBlur = new EventEmitter();
    @Output() editorFocus = new EventEmitter();

    @ViewChild('editor') editor;

    editorModes: Array<string>;
    editorMode;

    editorThemes: Array<string> = ['monokai', 'eclipse', 'merbivore'];
    editorTheme: string = this.editorThemes[0];

    constructor() {

    }

    ngOnInit() {
        this.editor.getEditor().on('blur', () =>  {
            this.editorBlur.emit(event);
        });
        this.editor.getEditor().on('focus', () =>  {
            this.editorFocus.emit(event);
        });

        if (this.modes) {
            this.editorModes = this.modes;
            this.editorMode  = this.modes[0];
        } else {
            this.editorModes = ['json', 'hjson'];
            this.editorMode = 'hjson';
        }

        this.editor.mode = this.editorMode;
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
            timer(100).subscribe(() => {
                this.editor.getEditor().clearSelection();
                this.editor.getEditor().focus();
                this.editor.getEditor().moveCursorTo(0,0);
            });
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

