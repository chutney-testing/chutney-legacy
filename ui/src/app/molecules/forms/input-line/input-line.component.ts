import { Component, Input, Output, EventEmitter } from '@angular/core';

@Component({
selector: 'chutney-forms-input-line',
templateUrl: './input-line.component.html',
styleUrls: ['./input-line.component.scss']
})
export class InputLineComponent {

    @Input() id: string;
    @Input() label: string;
    @Input() placeholder: string;
    @Input() type = 'text';
    @Input() model: string;
    @Output() modelChange = new EventEmitter<string>();
    @Input() validate: (value: string) => boolean = (_) => true;

    constructor() { }

    onInputChange(newValue: string) {
        this.model = newValue;
        this.modelChange.emit(this.model);
    }
}
