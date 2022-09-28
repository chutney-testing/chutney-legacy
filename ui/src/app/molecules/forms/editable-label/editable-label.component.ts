import { Component, Input, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'chutney-forms-editable-label',
  templateUrl: './editable-label.component.html',
  styleUrls: ['./editable-label.component.scss']
})
export class EditableLabelComponent {


    @Input() id: string;
    @Input() placeholder: string;
    @Input() type = 'simple';
    @Input() model: string;
    @Input() maxlength = 150;
    @Input() defaultValue = '';
    @Output() modelChange = new EventEmitter<string>();

    constructor() { }

    onInputChange(newValue: string) {
        this.model = newValue;
        this.modelChange.emit(this.model);
    }
}
