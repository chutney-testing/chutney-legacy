/**
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { AlertService } from '@shared';

import { InfoService, LoginService } from '@core/services';

@Component({
  selector: 'chutney-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
})
export class LoginComponent implements OnDestroy, OnInit {

  username: string;
  password: string;
  connectionError: string;
  action: string;

  private forwardUrl: string;
  private paramsSubscription: Subscription;
  private queryParamsSubscription: Subscription;
  version = '';
  applicationName = '';

  constructor(
    private loginService: LoginService,
    private infoService: InfoService,
    private route: ActivatedRoute,
    private alertService: AlertService,
  ) {
    this.paramsSubscription = this.route.params.subscribe(params => {
      this.action = params['action'];
    });
    this.queryParamsSubscription = this.route.queryParams.subscribe(params => {
      this.forwardUrl = params['url'];
    });
    this.infoService.getVersion().subscribe(result => {
      this.version = result;
    });
    this.infoService.getApplicationName().subscribe(result => {
      this.applicationName = result;
    });
  }

  ngOnInit() {
    if (this.loginService.isAuthenticated()) {
        this.loginService.navigateAfterLogin();
    }
  }

  ngOnDestroy() {
    if (this.paramsSubscription) {
        this.paramsSubscription.unsubscribe();
    }
    if (this.queryParamsSubscription) {
        this.queryParamsSubscription.unsubscribe();
    }
  }

  login() {
    this.loginService.login(this.username, this.password)
      .subscribe(
        (user) => {
          this.loginService.navigateAfterLogin(this.forwardUrl);
          this.alertService.removeAll();
        },
        (error) => {
            this.connectionError = error.error.message;
            this.action = null;
        }
      );
  }
}
