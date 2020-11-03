import { Component, Input } from '@angular/core';

@Component({
    selector: 'chutney-authoring-info',
    templateUrl: './authoring-info.component.html'
})
export class AuthoringInfoComponent {
    @Input() testCase;

    constructor() {
    }
}
