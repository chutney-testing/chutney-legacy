import { Component, HostBinding, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { User } from '@model';
import { LoginService } from '@core/services';
import { ThemeService } from '@core/theme/theme.service';
import { Theme } from '@core/theme/theme';
import { LayoutOptions } from '@core/layout/layout-options.service';

@Component({
    selector: 'chutney-chutney-main-header',
    templateUrl: './chutney-main-header.component.html',
    styleUrls: ['./chutney-main-header.component.scss']
})
export class ChutneyMainHeaderComponent implements OnInit {

    public user$: Observable<User>;
    FLATLY: string = Theme.FLATLY;
    DARKLY: string = Theme.DARKLY;

    constructor(private loginService: LoginService,
                private themeService: ThemeService,
                public globals: LayoutOptions) {
        this.user$ = this.loginService.getUser();
    }

    @HostBinding('class.isActive')
    get isActiveAsGetter() {
        return this.isActive;
    }

    isActive: boolean;


    toggleSidebarMobile() {
        this.globals.toggleSidebarMobile = !this.globals.toggleSidebarMobile;
    }

    ngOnInit(): void {
    }

    logout() {
        this.loginService.logout();
    }

    public switchTheme(theme: string) {
        this.themeService.switchTheme(theme);
    }

    public isCurrentTheme(theme: string): boolean {
        let currentTheme = this.themeService.getCurrentTheme();
        console.log('current theme: ' + currentTheme)
        console.log('passed theme: ' + theme)
        return currentTheme === theme;
    }
}
