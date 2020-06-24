import { Component, OnInit, ViewChild } from '@angular/core';
import { AlertService } from '@shared';
import { Observable } from 'rxjs';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import { HomePage } from '@model';
import { HomePageService } from '@core/services';

@Component({
  selector: 'chutney-home-page-create-dialog',
  templateUrl: './home-page-create-dialog.component.html',
  styleUrls: ['./home-page-create-dialog.component.scss']
})
export class HomePageCreateDialogComponent implements OnInit {

  homePage: HomePage;
  isSaving: boolean;

  private resizeInit = 0;

  constructor(public activeModal: NgbActiveModal,
    private alertService: AlertService,
    private homePageService: HomePageService) {
  }

  ngOnInit() {
    this.isSaving = false;

    const obs: Observable<HomePage> = this.homePageService.loadHomePage();
    obs.subscribe((homePageResult) => {
      this.homePage = homePageResult;
    },
      (error) => {
        console.log(error);
      }
    );
  }

  clear() {
    this.activeModal.dismiss('cancel');
  }

  save() {
    this.isSaving = true;
    this.subscribeToSaveResponse(
      this.homePageService.save(this.homePage));
  }

  onHomePageContentChanged(data: string): void {
    this.homePage.content = data;
  }

  private subscribeToSaveResponse(result: Observable<HomePage>) {
    result.subscribe((res: HomePage) =>
      this.onSaveSuccess(res), (res: Response) =>
        this.onSaveError(res));
  }

  private onSaveSuccess(result: HomePage) {
    this.isSaving = false;
    this.activeModal.dismiss(result);
  }

  private onSaveError(error) {
    try {
      error.json();
    } catch (exception) {
      error.message = error.text();
    }
    this.isSaving = false;
    this.onError(error);
  }

  private onError(error) {
    this.alertService.error(error.message);
  }

}
