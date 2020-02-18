import { Component, Input } from '@angular/core';

@Component({
selector: 'chutney-button',
template: `
    <button class="{{level}}" [disabled]="disabled">
        <span *ngIf="iconClass" class="fa {{iconClass}}"></span>
        {{model}}
    </button>
`,
styleUrls: ['./button.component.scss']
})
export class ButtonComponent {

    @Input() model: string;
    @Input() iconClass: string;
    @Input() level = 'first';
    @Input() disabled = false;

    constructor() { }
}
