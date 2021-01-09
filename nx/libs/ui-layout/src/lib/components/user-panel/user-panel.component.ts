import { Component, OnInit } from '@angular/core';
import { User, UserGQL } from '@chutney/data-access';
import { Observable } from 'rxjs';
import { pluck } from 'rxjs/operators';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';

@Component({
  selector: 'chutney-user-panel',
  templateUrl: './user-panel.component.html',
  styleUrls: ['./user-panel.component.scss'],
})
export class UserPanelComponent implements OnInit {
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
