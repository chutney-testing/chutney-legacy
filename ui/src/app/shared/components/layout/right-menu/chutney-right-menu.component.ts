import { Component, Input, OnInit } from '@angular/core';
import { Authorization } from '@model';
import { MenuItem } from '@shared/components/layout/menuItem';

@Component({
  selector: 'chutney-chutney-right-menu',
  templateUrl: './chutney-right-menu.component.html',
  styleUrls: ['./chutney-right-menu.component.scss']
})
export class ChutneyRightMenuComponent implements OnInit {

    @Input() menuItems: MenuItem [] = [];
    constructor() { }

    ngOnInit(): void {
        console.log(this.menuItems);
    }
}
