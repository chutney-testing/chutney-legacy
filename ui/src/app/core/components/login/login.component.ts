import { Component, OnInit } from '@angular/core';
import { LoginService } from '@core/services';
import { Router, ActivatedRoute } from '@angular/router';

@Component({
  selector: 'chutney-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
})
export class LoginComponent implements OnInit {

  username: string;
  password: string;
  connectionError: string;
  returnUrl: string;
  disconnected = false;

  constructor(
    private loginService: LoginService,
    private router: Router,
    private route: ActivatedRoute,
  ) {

    this.route.params.subscribe(params => {
      const action = params['action'];
      if (action === 'logout') {
        this.loginService.logout();
        this.disconnected = true;
      }
    });
  }

  ngOnInit() {
    this.returnUrl = this.route.snapshot.queryParams['url'] || '/scenario';
  }

  login() {
    this.loginService.login(this.username, this.password)
      .subscribe(
        data => { this.router.navigateByUrl(this.returnUrl); },
        error => { this.connectionError = error.error.message; }
      );
  }
}
