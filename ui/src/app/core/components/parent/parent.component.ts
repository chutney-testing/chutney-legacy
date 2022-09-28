import { Component, OnDestroy, OnInit } from '@angular/core';
import { Subscription } from 'rxjs';
import { LinkifierService, LoginService } from '@core/services';
import { LayoutOptions } from '@core/layout/layout-options.service';

@Component({
  selector: 'chutney-parent',
  templateUrl: './parent.component.html',
  styleUrls: ['./parent.component.scss']
})
export class ParentComponent implements OnInit, OnDestroy {

    private linkifierSubscription: Subscription;

  constructor(public layoutOptions: LayoutOptions,
    private linkifierService: LinkifierService,
    private loginService: LoginService) {
      this.linkifierSubscription = this.loginService.getUser().subscribe(
          user => {
              if (this.loginService.isAuthenticated()) {
                  this.linkifierService.loadLinkifiers().subscribe(); // needed to fetch linkifiers into sessionStorage
              }
          }
      );
  }

  ngOnInit(): void {
  }

  toggleSidebarMobile() {
        this.layoutOptions.toggleSidebarMobile = !this.layoutOptions.toggleSidebarMobile;
  }

    ngOnDestroy() {
        if (this.linkifierSubscription) {
            this.linkifierSubscription.unsubscribe();
        }
    }



}
