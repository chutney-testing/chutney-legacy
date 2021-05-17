import { Component, Input, OnInit } from '@angular/core';


import {  ComponentTask } from '@model';

@Component({
    selector: 'chutney-component-read',
    templateUrl: './component-read.component.html',
    styleUrls: ['./component-read.component.scss']
})
export class ComponentReadComponent implements OnInit {

    @Input() step: ComponentTask;
    showImplementation = false;
    ngOnInit(): void {

    }

    isNotEmpty(object: Object) {
        return Object.keys(object).length > 0;
    }
}
