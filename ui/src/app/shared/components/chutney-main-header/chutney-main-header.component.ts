import { Component, HostBinding, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { User } from '@model';
import { LoginService } from '@core/services';
import { ThemeService } from '@core/theme/theme.service';
import { Theme } from '@core/theme/theme';
import { LayoutOptions } from '../../../layout-options.service';

@Component({
  selector: 'chutney-chutney-main-header',
  templateUrl: './chutney-main-header.component.html',
  styleUrls: ['./chutney-main-header.component.scss']
})
export class ChutneyMainHeaderComponent implements OnInit {
    public user$: Observable<User>;
    public Theme = Theme;
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

    public switchTheme(theme: Theme) {
        this.themeService.switchTheme(theme);
    }

    public isCurrentTheme(theme: Theme): boolean {
        return this.themeService.getCurrentTheme() === theme;
    }
}
