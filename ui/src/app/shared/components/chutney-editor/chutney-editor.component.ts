import {
    AfterViewInit,
    Component,
    ElementRef,
    EventEmitter,
    Input,
    OnChanges,
    OnInit,
    Output,
    SimpleChanges,
    ViewChild
} from '@angular/core';
import * as ace from 'ace-builds';
import { Ace } from 'ace-builds';

@Component({
    selector: 'chutney-editor',
    templateUrl: './chutney-editor.component.html',
    styleUrls: ['./chutney-editor.component.scss']
})
export class ChutneyEditorComponent implements OnInit, AfterViewInit, OnChanges {

    @Input() content = '';
    @Input() modes: string[];
    @Input() mode: string;
    currentMode: string;
    @Output() onContentChange = new EventEmitter<string>();

    themes: Array<string> = ['twilight', 'tomorrow'];
    currentTheme = this.themes[0];
    options: { theme: string, language: string }

    @ViewChild('editor')
    private editorHtmlElement: ElementRef<HTMLElement>;
    private aceEditor: Ace.Editor;

    constructor() {
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (this.aceEditor){
            this.initEditor();
        }
    }

    ngOnInit(): void {
        if (!this.mode) {
            this.mode = this.modes[0];
        }
        this.currentMode = this.mode;
    }

    ngAfterViewInit(): void {
        this.initEditor();
    }

    changeTheme(event: any) {
        this.currentTheme = event.target.value;
        this.aceEditor.setTheme(`ace/theme/${this.currentTheme}`);
    }

    changeMode(event: any) {
        this.currentMode = event.target.value;
        this.aceEditor.session.setMode(`ace/mode/${this.currentMode}`);
    }

    private initEditor() {
        ace.config.set('basePath', 'https://unpkg.com/ace-builds@1.4.12/src-noconflict/');
        this.aceEditor = ace.edit(this.editorHtmlElement.nativeElement);
        this.aceEditor.session.setValue(this.content ? this.content : '');
        this.aceEditor.setTheme(`ace/theme/${this.currentTheme}`);
        this.aceEditor.session.setMode(`ace/mode/${this.mode}`);
        this.aceEditor.on('change', () => this.onContentChange.emit(this.aceEditor.getValue()))
    }
}
