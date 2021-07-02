import { Injectable } from '@angular/core';
import { CanDeactivate } from '@angular/router';
import { CanDeactivatePage } from './page';
import { TranslateService } from '@ngx-translate/core';

@Injectable({
  providedIn: 'root'
})
export class CanDeactivateGuard implements CanDeactivate<CanDeactivatePage> {
  confirmationText: string;

  constructor(private translate: TranslateService) {
    this.initTranslation();
  }

  canDeactivate(page: CanDeactivatePage): boolean {
    if (page && page.canDeactivatePage && !page.canDeactivatePage()) {
      return confirm(this.confirmationText);
    }
    return true;
  }

  private initTranslation() {
    this.getTranslation();
    this.translate.onLangChange.subscribe(() => {
        this.getTranslation();
    });
  }

  private getTranslation() {
    this.translate.get('global.confirm.page.deactivate').subscribe((res: string) => {
      this.confirmationText = res;
    });
  }
}
