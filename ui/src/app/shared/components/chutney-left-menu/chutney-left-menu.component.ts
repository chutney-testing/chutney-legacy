import { Component, HostListener, OnInit } from '@angular/core';
import { LoginService } from '@core/services';
import { allMenuItems, MenuItem } from '@shared/components/chutney-left-menu/chutney-left-menu.items';
import { LayoutOptions } from '@core/layout/layout-options.service';

@Component({
    selector: 'chutney-chutney-left-menu',
    templateUrl: './chutney-left-menu.component.html',
    styleUrls: ['./chutney-left-menu.component.scss']
})
export class ChutneyLeftMenuComponent implements OnInit {
    public menuItems = allMenuItems;
    private newInnerWidth: number;
    private innerWidth: number;

    constructor(public layoutOptions: LayoutOptions,
                private loginService: LoginService) {
    }

    ngOnInit(): void {
        setTimeout(() => {
            this.innerWidth = window.innerWidth;
            if (this.innerWidth < 1200) {
                this.layoutOptions.toggleSidebar = true;
            }
        });
    }

    canViewMenuGroup(item: MenuItem): boolean {
        return !!item.children.find(subItem => this.canViewMenuItem(subItem));

    }

    canViewMenuItem(item: MenuItem): boolean {
        return this.loginService.hasAuthorization(item.authorizations);
    }

    toggleSidebar() {
        this.layoutOptions.toggleSidebar = !this.layoutOptions.toggleSidebar;
    }

    sidebarHover() {
        this.layoutOptions.sidebarHover = !this.layoutOptions.sidebarHover;
    }

    @HostListener('window:resize', ['$event'])
    onResize(event) {
        this.newInnerWidth = event.target.innerWidth;

        if (this.newInnerWidth < 1200) {
            this.layoutOptions.toggleSidebar = true;
        } else {
            this.layoutOptions.toggleSidebar = false;
        }

    }
}
