import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { User, UserGQL } from '@chutney/data-access';
import { pluck } from 'rxjs/operators';

@Component({
  selector: 'chutney-user-menu',
  templateUrl: './user-menu.component.html',
  styleUrls: ['./user-menu.component.scss'],
})
export class UserMenuComponent implements OnInit {
  user$: Observable<User>;

  constructor(private userGQL: UserGQL) {}

  ngOnInit(): void {
    this.user$ = this.userGQL.watch().valueChanges.pipe(pluck('data', 'user'));
  }
}
