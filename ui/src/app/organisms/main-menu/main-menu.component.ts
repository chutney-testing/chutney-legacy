import { Component, OnInit } from '@angular/core';
import { LoginService } from '@core/services';
import { User } from '@model';

@Component({
  selector: 'chutney-main-menu',
  templateUrl: './main-menu.component.html',
  styleUrls: ['./main-menu.component.scss']
})
export class MainMenuComponent implements OnInit {

  ICON_TESTS = require('../../../assets/icons/tests.png');
  ICON_TESTS_SELECTED = require('../../../assets/icons/tests-selected.png');

  ICON_REPO = require('../../../assets/icons/repository.png');
  ICON_REPO_SELECTED = require('../../../assets/icons/repository-selected.png');

  currentUser: User = null;

  constructor(
    private loginService: LoginService
  ) { }

  ngOnInit() {
    const user = this.loginService.getUser();
    if (user) {
      this.currentUser = user;
    }
  }
}
