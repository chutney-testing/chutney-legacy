import {AfterViewInit, Component, EventEmitter, Input, OnChanges, Output, ViewChild} from '@angular/core';
import 'brace';
import 'brace/mode/json';
import 'brace/mode/hjson';
import 'brace/mode/html';

@Component({
    selector: 'chutney-ace-editor',
    templateUrl: './ace-editor.component.html',
    styleUrls: ['./ace-editor.component.scss']
})
export class AceEditorComponent implements AfterViewInit, OnChanges {

    @Input() initialContent: string;
    @Input() hasError: boolean;
    @Output() textChangeEvent = new EventEmitter();

    @ViewChild('editor') editor;
    editorModes: Array<EditorMode> = [
        new EditorMode('Json', 'json'), new EditorMode('Hjson', 'hjson')
    ];
    editorMode = this.editorModes[1];

    constructor() {
    }


    ngAfterViewInit() {
        this.editor.getEditor().setOptions({
            showLineNumbers: true,
            tabSize: 2
        });
        console.log(this.editorMode.name);
        this.editor.mode = this.editorMode.name;
        this.editor.options = {
            fontSize: '13pt',
            showPrintMargin: false
        };
    }

    ngOnChanges(): void {
        if (this.editor.value.length === 0) {
            this.editor.value = this.initialContent;
        }
    }

    changingMode(event: any) {
        this.editorMode = this.editorModes.filter(env => env.name === event.target.value)[0];
        this.editor.mode = this.editorMode.name;
    }

    onContentChange(event: any) {
        this.textChangeEvent.emit(event);
    }


}

class EditorMode {
    constructor(public label: string, public name: string) {
    }
}
