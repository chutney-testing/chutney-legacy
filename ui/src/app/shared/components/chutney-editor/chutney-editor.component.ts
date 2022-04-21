import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';

@Component({
    selector: 'chutney-editor',
    templateUrl: './chutney-editor.component.html',
    styleUrls: ['./chutney-editor.component.scss']
})
export class ChutneyEditorComponent implements OnInit {

    @Input() content = '';
    @Input() language: string
    @Output() onContentChange = new EventEmitter<string>()

    themes: Array<string> = ['vs', 'vs-dark', 'hc-black'];
    currentTheme =  this.themes[1];
    options: {theme: string, language: string}

    constructor() {
    }

    ngOnInit(): void {
        this.options = {theme: this.currentTheme, language: this.language};
    }

    changeTheme(event: any) {
        this.currentTheme = event.target.value;
        this.options = { ...this.options, theme: this.currentTheme };
    }

    contentChange() {
        this.onContentChange.emit(this.content)
    }

}
