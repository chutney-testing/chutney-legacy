/**
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import 'ace-builds/webpack-resolver';

@Component({
    selector: 'chutney-editor',
    templateUrl: './chutney-editor.component.html',
    styleUrls: ['./chutney-editor.component.scss']
})
export class ChutneyEditorComponent implements OnInit, AfterViewInit, OnChanges {

    @Input() content = '';
    @Input() modes: string[];
    @Input() mode: string;
    @Input() height = '420px';
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
        this.aceEditor = ace.edit(this.editorHtmlElement.nativeElement);
        this.aceEditor.session.setValue(this.content ? this.content : '');
        this.aceEditor.setTheme(`ace/theme/${this.currentTheme}`);
        this.aceEditor.session.setMode(`ace/mode/${this.mode}`);
        this.aceEditor.on('change', () => this.onContentChange.emit(this.aceEditor.getValue()))
    }
}
