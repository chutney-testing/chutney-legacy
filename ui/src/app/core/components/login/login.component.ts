import { Component, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';

import { LoginService } from '@core/services';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { NzMessageService } from 'ng-zorro-antd/message';

@Component({
  selector: 'chutney-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
})
export class LoginComponent implements OnDestroy {

  connectionError: string;
  action: string;

  private forwardUrl: string;
  private paramsSubscription: Subscription;
  private queryParamsSubscription: Subscription;
  validateForm!: FormGroup;


  constructor(
    private loginService: LoginService,
    private route: ActivatedRoute,
    private fb: FormBuilder,
    private message: NzMessageService
  ) {

    this.paramsSubscription = this.route.params.subscribe(params => {
      this.action = params['action'];
    });

    this.queryParamsSubscription = this.route.queryParams.subscribe(params => {
      this.forwardUrl = params['url'];
    });
  }


  submitForm(): void {
    if (this.validateForm.valid) {
      this.login();
    } else {
      Object.values(this.validateForm.controls).forEach(control => {
        if (control.invalid) {
          control.markAsDirty();
          control.updateValueAndValidity({ onlySelf: true });
        }
      });
    }
  }


  ngOnInit(): void {
    this.validateForm = this.fb.group({
      userName: [null, [Validators.required]],
      password: [null, [Validators.required]],
      remember: [true]
    });
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
      const username = this.validateForm.controls['userName'].value;
      const password = this.validateForm.controls['password'].value;
    this.loginService.login(username, password)
      .subscribe(
        next => this.loginService.navigateAfterLogin(this.forwardUrl),
        error => {
            this.connectionError = error.error.message;
            this.message.error(this.connectionError);
            this.action = null;
        }
      );
  }
}
