import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { User, Authorization } from '@model';
import { LoginService } from '@core/services';
import { Router } from '@angular/router';
import { ThemeService } from '@core/theme/theme.service';
import { Theme } from '@core/theme/theme';

@Component({
  selector: 'chutney-chutney-main-header',
  templateUrl: './chutney-main-header.component.html',
  styleUrls: ['./chutney-main-header.component.scss']
})
export class ChutneyMainHeaderComponent implements OnInit {
    public user$: Observable<User>;
    public Theme = Theme;
  constructor(private loginService: LoginService,
              private themeService: ThemeService) {
      this.user$ = this.loginService.getUser();
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
