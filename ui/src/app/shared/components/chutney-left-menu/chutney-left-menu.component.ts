import { Component, OnInit } from '@angular/core';
import { LoginService } from '@core/services';
import { allMenuItems } from '@shared/components/chutney-left-menu/chutney-left-menu.items';

@Component({
    selector: 'chutney-chutney-left-menu',
    templateUrl: './chutney-left-menu.component.html',
    styleUrls: ['./chutney-left-menu.component.scss']
})
export class ChutneyLeftMenuComponent implements OnInit {
    public isCollapsed = false;
    public menuItems: Array<{ label, link, iconClass, authorizations }> = [];

    constructor(private loginService: LoginService) {
    }

    ngOnInit(): void {
        this.menuItems = allMenuItems.filter(item => this.loginService.hasAuthorization(item.authorizations))
    }

    toggleExpand() {
        this.isCollapsed = !this.isCollapsed;
    }
}
