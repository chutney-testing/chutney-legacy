import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { ToastrService } from 'ngx-toastr';

@Injectable({
  providedIn: 'root'
})
export class AlertService {

  private successTitle: string = '';
  private infoTitle: string = '';
  private errorTitle: string = '';
  private warningTitle: string = '';

  constructor(
    private toastr: ToastrService,
    private translateService: TranslateService
  ) {
    this.initTranslation();
  }

  success(msg: string, config = {}) {
    this.toastr.success(msg, this.successTitle, config);
  }

  info(msg: string, config = {}) {
    this.toastr.info(msg, this.infoTitle, config);
  }

  error(msg: string, config = {}) {
    this.toastr.error(msg, this.errorTitle, config);
  }

  warning(msg: string, config = {}) {
    this.toastr.warning(msg, this.warningTitle, config);
  }

  private initTranslation() {
    this.getTranslation();
    this.translateService.onLangChange.subscribe(() => {
        this.getTranslation();
    });
  }

  private getTranslation() {
    this.translateService.get('alert.success').subscribe((res: string) => {
        this.successTitle = res;
    });
    this.translateService.get('alert.info').subscribe((res: string) => {
        this.infoTitle = res;
    });
    this.translateService.get('alert.error').subscribe((res: string) => {
        this.errorTitle = res;
    });
    this.translateService.get('alert.warning').subscribe((res: string) => {
        this.warningTitle = res;
    });
  }
}
