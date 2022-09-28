import { Component, Input, Output, EventEmitter } from '@angular/core';

@Component({
selector: 'chutney-forms-input',
template: `
    <input
        id="{{id}}"
        name="{{id}}"
        type="{{isPassword(id)? 'password' : 'text'}}"
        placeholder="{{placeholder}}"
        [ngModel]="model"
        (ngModelChange)="onInputChange($event)"
        class="form-control"
        [ngClass]="{'invalid': !validate(model)}"
    />
`,
styleUrls: ['./input.component.scss']
})
export class InputComponent {

    @Input() id: string;
    @Input() placeholder: string;
    @Input() type = 'text';
    @Input() model: string;
    @Output() modelChange = new EventEmitter<string>();
    @Input() validate: (value: string) => boolean = (_) => true;
    @Input() hidePassword: boolean = false;

    constructor() { }

    onInputChange(newValue: string) {
        this.model = newValue;
        this.modelChange.emit(this.model);
    }

    isPassword(key: string): boolean{
        var keywordList = ['password','pwd'];
        var isPassword;
        keywordList.forEach( (keyword) => {
            isPassword ||= key.toLowerCase().includes(keyword);
        });
        return this.hidePassword && isPassword;
    }

}
