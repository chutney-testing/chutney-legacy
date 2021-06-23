import { Component, OnInit, OnDestroy, ViewEncapsulation } from '@angular/core';
import { Subscription } from 'rxjs';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

import { HomePageCreateDialogComponent } from '../home-page-create/home-page-create-dialog.component';
import { EventManagerService } from '@shared';
import { HomePageService } from '@core/services';
import { HomePage, Authorization } from '@model';

@Component({
  selector: 'chutney-home-page',
  templateUrl: './home-page.component.html',
  styleUrls: ['./home-page.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class HomePageComponent implements OnInit, OnDestroy {

  public homePage: HomePage;
  private homePageModifedSubscription: Subscription;

  Authorization = Authorization;

  constructor(
    private homePageService: HomePageService,
    private eventManager: EventManagerService,
    private modalService: NgbModal
  ) { }

  ngOnInit() {
    this.homePageService.loadHomePage().subscribe((homePage) => this.initHomePage(homePage));
    this.homePageModifedSubscription = this.eventManager.subscribe('homePageModified', (event) => this.initHomePage(event.content));
  }

  ngOnDestroy() {
    this.eventManager.destroy(this.homePageModifedSubscription);
  }

  private initHomePage(homePage: HomePage): void {
    this.homePage = homePage;
  }

  openHomePageDialog() {
    this.modalService.open(HomePageCreateDialogComponent, { size: 'lg', backdrop: 'static', windowClass: 'scenario-modal' });
  }
}
