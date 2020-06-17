import { Component, OnInit, ViewChild } from '@angular/core';
import { AlertService } from '@shared';
import { Observable } from 'rxjs';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import { HomePage } from '@model';
import { HomePageService } from '@core/services';

@Component({
  selector: 'chutney-home-page-create-dialog',
  templateUrl: './home-page-create-dialog.component.html'
})
export class HomePageCreateDialogComponent implements OnInit {

  editorTheme: EditorTheme = new EditorTheme('Monokai', 'monokai');
  editorMode: EditorMode = new EditorMode('AsciiDoc', 'asciidoc');

  homePage: HomePage;
  isSaving: boolean;
  aceOptions: any = {
    fontSize: '13pt',
    enableBasicAutocompletion: true,
    showPrintMargin: false
  };

  private resizeInit = 0;

 // @ViewChild(AceEditorDirective) aceEditorDirective: AceEditorDirective;

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

  resizeEditor() {
    if (this.resizeInit === 0) {
      const contentClientHeight = document.getElementsByClassName('modal-content')[0].clientHeight;
      const headerClientHeight = document.getElementsByClassName('modal-header')[0].clientHeight + 1;
      const footerClientHeight = document.getElementsByClassName('modal-footer')[0].clientHeight + 1;

      this.resizeInit = contentClientHeight - (headerClientHeight + footerClientHeight) - 10;
    }
    document.getElementById('editor').style.height = this.resizeInit + 'px';
    (document.getElementsByClassName('modal-body')[0] as HTMLElement).style.height = this.resizeInit + 10 + 'px';
   // this.aceEditorDirective.editor.resize();
  }

}

class EditorMode {
  constructor(public label: string, public name: string) {
  }
}

class EditorTheme {
  constructor(public label: string, public name: string) {
  }
}
