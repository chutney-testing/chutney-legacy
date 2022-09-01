import { Component } from '@angular/core';
import { Subscription } from 'rxjs';

import { LinkifierService, LoginService } from '@core/services';

@Component({
  selector: 'chutney-parent',
  templateUrl: './parent.component.html',
  styleUrls: ['./parent.style.scss'],
})
export class ParentComponent {

  private linkifierSubscription: Subscription;

  constructor(
    private linkifierService: LinkifierService,
    private loginService: LoginService
  ) {
    this.linkifierSubscription = this.loginService.getUser().subscribe(
        user => {
            if (this.loginService.isAuthenticated()) {
                this.linkifierService.loadLinkifiers().subscribe(); // needed to fetch linkifiers into sessionStorage
            }
        }
    );
  }

  ngOnDestroy() {
    if (this.linkifierSubscription) {
        this.linkifierSubscription.unsubscribe();
    }
  }
}
