import { Component, Input } from '@angular/core';
import { MenuItem } from '@shared/components/layout/menuItem';

@Component({
  selector: 'chutney-chutney-right-menu',
  templateUrl: './chutney-right-menu.component.html',
  styleUrls: ['./chutney-right-menu.component.scss']
})
export class ChutneyRightMenuComponent {

    @Input() menuItems: MenuItem [] = [];
    constructor() { }

    onItemClick(item: MenuItem) {
        if (item.click) {
            const option = item.options ? item.options[0].id : null;
            option ? item.click(option) : item.click();
        }
    }

    getItemLink(item: MenuItem) {
        return item.link ? [item.link]: []
    }
}
