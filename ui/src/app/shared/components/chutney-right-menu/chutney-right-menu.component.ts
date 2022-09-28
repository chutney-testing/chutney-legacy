import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'chutney-chutney-right-menu',
  templateUrl: './chutney-right-menu.component.html',
  styleUrls: ['./chutney-right-menu.component.scss']
})
export class ChutneyRightMenuComponent implements OnInit {

    expanded = true;
    menuItems: Array<{label, link, iconClass}> = [
    ];
    constructor() { }

    ngOnInit(): void {
    }
    toggleExpand() {
        this.expanded = !this.expanded;
    }
}
