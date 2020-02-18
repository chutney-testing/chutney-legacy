import { Directive, ElementRef, EventEmitter, Input, Output } from '@angular/core';

import * as ace from 'brace';
import 'brace/ext/language_tools.js';
import 'brace/ext/searchbox';
import 'brace/theme/monokai';
import 'brace/theme/eclipse';
import 'brace/theme/terminal';
import 'brace/mode/gherkin';
import 'brace/mode/json';
import 'brace/mode/hjson';
import 'brace/mode/html';
import 'brace/mode/asciidoc';
import { fromEvent } from 'rxjs';
import { map, debounceTime, distinctUntilChanged } from 'rxjs/internal/operators';

@Directive({
  selector: '[chutneyAceEditor]'
})
export class AceEditorDirective {
  _readOnly: any;
  _theme: any;
  _mode: any;

  editor: any;
  onChangeText: boolean = false;

  @Input() set options(value) {
    this.editor.setOptions(value || {});
  }

  @Input() set readOnly(value) {
    this._readOnly = value;
    this.editor.setReadOnly(value);
  }

  @Input() set theme(value) {
    this._theme = value;
    this.editor.setTheme(`ace/theme/${value}`);
  }

  @Input() set mode(value) {
    this._mode = value;
    this.editor.getSession().setMode(`ace/mode/${value}`);
  }


  @Input() set text(value) {
    if (this.onChangeText) { return; }
    this.editor.setValue(value);
    this.editor.clearSelection();
    this.editor.focus();
  }

  @Output() textChanged = new EventEmitter();
  @Output() editorRef = new EventEmitter();
  @Output() editorFocus = new EventEmitter();
  @Output() editorBlur = new EventEmitter();

  constructor(private elementRef: ElementRef) {
    const el = elementRef.nativeElement;
    el.classList.add('editor');

    this.editor = ace.edit(el);
    this.editor.$blockScrolling = Infinity;

    setTimeout(() => {
      this.editorRef.next(this.editor);
    });

    fromEvent(this.editor, 'change').pipe(
      map(event => this.editor.getValue()),
      debounceTime(1000),
      distinctUntilChanged()
    ).subscribe((value) => {
      this.onChangeText = true;
      this.textChanged.next(value);
    });

    fromEvent(this.editor, 'focus').subscribe(event => this.editorFocus.emit(event));
    fromEvent(this.editor, 'blur').subscribe(event => this.editorBlur.emit(event));
  }
}
