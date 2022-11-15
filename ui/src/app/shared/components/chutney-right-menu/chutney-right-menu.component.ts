import { Component, Input, OnInit } from '@angular/core';
import { Authorization } from '@model';

@Component({
  selector: 'chutney-chutney-right-menu',
  templateUrl: './chutney-right-menu.component.html',
  styleUrls: ['./chutney-right-menu.component.scss']
})
export class ChutneyRightMenuComponent implements OnInit {

    expanded = true;
    @Input() menuItems: {
        label: string,
        click?: Function,
        link?: string,
        class?: string,
        authorizations:Authorization[]
    } [] = [];
    constructor() { }

    ngOnInit(): void {
    }
    toggleExpand() {
        this.expanded = !this.expanded;
    }
}
