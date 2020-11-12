import { Component, OnInit } from '@angular/core';
import { User, UserGQL } from '@chutney/data-access';
import { Observable } from 'rxjs';
import { pluck } from 'rxjs/operators';

@Component({
  selector: 'chutney-user-panel',
  templateUrl: './user-panel.component.html',
  styleUrls: ['./user-panel.component.scss'],
})
export class UserPanelComponent implements OnInit {
  user$: Observable<User>;
  user: User;

  constructor(private userGQL: UserGQL) {}

  ngOnInit(): void {
    this.user$ = this.userGQL.watch().valueChanges.pipe(pluck('data', 'user'));
  }
}
