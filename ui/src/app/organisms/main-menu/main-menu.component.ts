import { Component } from '@angular/core';
import { Observable } from 'rxjs';

import { LoginService } from '@core/services';
import { User } from '@model';

@Component({
  selector: 'chutney-main-menu',
  templateUrl: './main-menu.component.html',
  styleUrls: ['./main-menu.component.scss']
})
export class MainMenuComponent {

  ICON_TESTS = require('../../../assets/icons/tests.png');
  ICON_TESTS_SELECTED = require('../../../assets/icons/tests-selected.png');

  ICON_REPO = require('../../../assets/icons/repository.png');
  ICON_REPO_SELECTED = require('../../../assets/icons/repository-selected.png');

  user$: Observable<User>;

  constructor(
    private loginService: LoginService
  ) {
    this.user$ = this.loginService.getUser();
  }
}
