import { Component, Input, OnInit } from '@angular/core';


import {  FunctionalStep } from '@model';

@Component({
    selector: 'chutney-gwt-read',
    templateUrl: './gwt-read.component.html',
    styleUrls: ['./gwt-read.component.scss']
})
export class GwtReadComponent implements OnInit {

    @Input() step: FunctionalStep;
    @Input() type: string;

    showImplementation = false;
    ngOnInit(): void {

    }

}
