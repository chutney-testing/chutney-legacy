import { Component, Input, OnInit } from '@angular/core';
import { Authorization } from '@model';

@Component({
  selector: 'chutney-chutney-right-menu',
  templateUrl: './chutney-right-menu.component.html',
  styleUrls: ['./chutney-right-menu.component.scss']
})
export class ChutneyRightMenuComponent implements OnInit {

    @Input() menuItems: {
        label: string,
        click?: Function,
        link?: string,
        class?: string,
        authorizations:Authorization[],
        options: {id: string,label: string} []
    } [] = [];
    constructor() { }

    ngOnInit(): void {
        console.log(this.menuItems);
    }
}
