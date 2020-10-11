import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { Authenticate } from '../../../../../data-models';
import { ActivatedRoute, Router } from '@angular/router';
import { LoginGQL, UserDocument } from '@chutney/data-access';
import { pluck } from 'rxjs/operators';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Apollo } from 'apollo-angular';

const formSerializer = (data: any, headers: Headers) => {
  const body = new URLSearchParams();
  body.set('username', data.username);
  body.set('password', data.password);
  headers.set('Content-Type', 'application/x-www-form-urlencoded');
  return { body, headers };
};

@Component({
  selector: 'chutney-testing-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginComponent implements OnInit {
  qs: { [key: string]: any };

  constructor(
    public snackBar: MatSnackBar,
    private route: ActivatedRoute,
    private apollo: Apollo,
    private loginGQL: LoginGQL,
    private router: Router
  ) {
    this.route.params.subscribe((params) => (this.qs = params));
  }

  ngOnInit() {}

  login(authenticate: Authenticate): void {
    this.loginGQL
      .mutate({
        input: {
          username: authenticate.username,
          password: authenticate.password,
        },
        bodySerializer: formSerializer,
      })
      .pipe(pluck('data', 'login'))
      .subscribe(
        (user) => {
          const data = { __typename: 'User', ...user };
          localStorage.setItem('user', JSON.stringify(data));
          this.apollo.client.writeQuery({ query: UserDocument, data: data });
          this.snackBar.open('Has logged', 'Login successful');
          this.router.navigate([this.qs.next || '/']);
        },
        (err) => {
          console.log(err);
          this.snackBar.open(err.message, 'Login failed');
        }
      );
  }
}
