import { Component, HostListener, OnInit } from '@angular/core';
import { LoginService } from '@core/services';
import { allMenuItems, MenuItem } from '@shared/components/chutney-left-menu/chutney-left-menu.items';
import { ActivatedRoute } from '@angular/router';
import { ThemeOptions } from '../../../theme-options';

@Component({
    selector: 'chutney-chutney-left-menu',
    templateUrl: './chutney-left-menu.component.html',
    styleUrls: ['./chutney-left-menu.component.scss']
})
export class ChutneyLeftMenuComponent implements OnInit {
    public isCollapsed = false;
    public menuItems = allMenuItems;
    public extraParameter: any;

    constructor(private loginService: LoginService,
                public globals: ThemeOptions, private activatedRoute: ActivatedRoute) {
    }

    ngOnInit(): void {
        //this.menuItems = allMenuItems.filter(item => this.loginService.hasAuthorization(item.authorizations))
        setTimeout(() => {
            this.innerWidth = window.innerWidth;
            if (this.innerWidth < 1200) {
                this.globals.toggleSidebar = true;
            }
        });

        //this.extraParameter = this.activatedRoute.snapshot.firstChild.data['extraParameter'];
        this.extraParameter = '';
    }

    private newInnerWidth: number;
    private innerWidth: number;
    activeId = 'dashboardsMenu';

    toggleSidebar() {
        this.globals.toggleSidebar = !this.globals.toggleSidebar;
    }

    sidebarHover() {
        this.globals.sidebarHover = !this.globals.sidebarHover;
    }

    @HostListener('window:resize', ['$event'])
    onResize(event) {
        this.newInnerWidth = event.target.innerWidth;

        if (this.newInnerWidth < 1200) {
            this.globals.toggleSidebar = true;
        } else {
            this.globals.toggleSidebar = false;
        }

    }
}
