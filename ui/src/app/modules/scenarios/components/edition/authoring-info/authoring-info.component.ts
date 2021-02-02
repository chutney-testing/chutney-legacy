import { Component, Input } from '@angular/core';

@Component({
    selector: 'chutney-authoring-info',
    templateUrl: './authoring-info.component.html',
    styleUrls: ['./authoring-info.component.scss']
})
export class AuthoringInfoComponent {
    @Input() testCase;

    constructor() {
    }
}
