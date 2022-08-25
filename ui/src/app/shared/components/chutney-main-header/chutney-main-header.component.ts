import { Component, HostBinding, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { User, Authorization } from '@model';
import { LoginService } from '@core/services';
import { Router } from '@angular/router';
import { ThemeService } from '@core/theme/theme.service';
import { Theme } from '@core/theme/theme';
import { ThemeOptions } from '../../../theme-options';

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
              public globals: ThemeOptions) {
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

    toggleHeaderMobile() {
        this.globals.toggleHeaderMobile = !this.globals.toggleHeaderMobile;
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
