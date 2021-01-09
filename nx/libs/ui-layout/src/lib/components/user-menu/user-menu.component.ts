import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { User, UserGQL } from '@chutney/data-access';
import { pluck } from 'rxjs/operators';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';

@Component({
  selector: 'chutney-user-menu',
  templateUrl: './user-menu.component.html',
  styleUrls: ['./user-menu.component.scss'],
})
export class UserMenuComponent implements OnInit {
  user$: Observable<User>;

  constructor(
    private userGQL: UserGQL,
    private snackBar: MatSnackBar,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.user$ = this.userGQL.watch().valueChanges.pipe(pluck('data', 'user'));
  }

  logout() {
    localStorage.removeItem('user');
    this.snackBar.open('Logged out', 'Exit successfully');
    this.router.navigate(['/auth/login']);
  }
}
