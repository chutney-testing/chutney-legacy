import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';

import { LoginService } from '@core/services';
import { User, Authorization } from '@model';

@Component({
    selector: 'chutney-main-menu',
    templateUrl: './main-menu.component.html',
    styleUrls: ['./main-menu.component.scss']
})
export class MainMenuComponent {
    user$: Observable<User>;
    Authorization = Authorization;

    constructor(
        private loginService: LoginService,
        private router: Router
    ) {
        this.user$ = this.loginService.getUser();
    }

    logout() {
        this.loginService.logout();
    }

    login() {
        this.router.navigate(['login']);
    }
}
