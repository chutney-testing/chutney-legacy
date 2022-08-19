import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { User, Authorization } from '@model';
import { LoginService } from '@core/services';
import { Router } from '@angular/router';

@Component({
  selector: 'chutney-chutney-main-header',
  templateUrl: './chutney-main-header.component.html',
  styleUrls: ['./chutney-main-header.component.scss']
})
export class ChutneyMainHeaderComponent implements OnInit {
    public user$: Observable<User>;
    public darkMode = false;
  constructor(private loginService: LoginService,) {
      this.user$ = this.loginService.getUser();
  }

  ngOnInit(): void {
  }
    logout() {
        this.loginService.logout();
    }

    public toggleDarkMode() {
      this.darkMode = !this.darkMode;
    }

}
