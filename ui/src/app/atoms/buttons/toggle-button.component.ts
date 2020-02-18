import { Component, Input } from '@angular/core';

@Component({
selector: 'chutney-toggle-button',
template: `
    <span>{{label}}<br>
        <input class="toggle toggle-{{style}}" id="{{id}}" type="checkbox" [checked]="isChecked"/>
        <label class="toggle-btn" for="{{id}}" attr.data-tg-on="{{onValue}}" attr.data-tg-off="{{offValue}}"></label>
    </span>
`,
styleUrls: ['./toggle-button.component.scss']
})
export class ToggleButtonComponent {

    @Input() label: string;
    @Input() onValue: string = "On";
    @Input() offValue: string = "Off";
    @Input() id: string;
    @Input() style: string; // light, skewed, flat
    @Input() isChecked: boolean = false;

    constructor() { }
}
