import { Component, OnInit, OnDestroy } from '@angular/core';
import { LoginService } from '@core/services';
import { Router, ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';

import { User, Authorization } from '@model';
import { contains } from '@shared/tools/array-utils';

@Component({
  selector: 'chutney-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
})
export class LoginComponent implements OnInit {

  username: string;
  password: string;
  connectionError: string;
  action: string;

  private forwardUrl: string;
  private userSubscription: Subscription;

  constructor(
    private loginService: LoginService,
    private router: Router,
    private route: ActivatedRoute,
  ) {

    this.route.params.subscribe(params => {
      this.action = params['action'];
      if (this.action) {
        this.loginService.logout();
      }
    });

    this.route.queryParams.subscribe(params => {
          this.forwardUrl = params['url'];
        });
  }

  ngOnInit() {
    this.forwardUrl = this.route.queryParams['url'];
  }

  ngOnDestroy() {
    if (this.userSubscription) this.userSubscription.unsubscribe();
  }

  login() {
    this.loginService.login(this.username, this.password)
      .subscribe(
        user => {
            this.router.navigateByUrl(this.defaultForwardUrl(user));
        },
        error => { this.connectionError = error.error.message; }
      );
  }

  private defaultForwardUrl(user: User): string {
    if (this.forwardUrl) {
        return this.forwardUrl;
    }

    const authorizations = user.authorizations;
    if (authorizations) {
        if (contains(authorizations, Authorization.SCENARIO_READ)) return '/scenario';
        if (contains(authorizations, Authorization.CAMPAIGN_READ)) return '/campaign';
        if (contains(authorizations, Authorization.COMPONENT_READ)) return '/component';
        if (contains(authorizations, Authorization.ENVIRONMENT_ACCESS)) return '/environmentAdmin';
        if (contains(authorizations, Authorization.GLOBAL_VAR_READ)) return '/variable';
        if (contains(authorizations, Authorization.DATASET_READ)) return '/dataset';
        if (contains(authorizations, Authorization.ADMIN_ACCESS)) return '/';
    }

    return '/home-page';
  }
}
