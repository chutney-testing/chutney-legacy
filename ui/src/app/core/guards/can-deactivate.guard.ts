import { Injectable } from "@angular/core";
import { CanDeactivate } from "@angular/router";
import { CanDeactivatePage } from "./page";
import { TranslateService } from "@ngx-translate/core";

@Injectable({
  providedIn: 'root'
})
export class CanDeactivateGuard implements CanDeactivate<CanDeactivatePage> {
  confirmationText: string;

  constructor(private translate: TranslateService) {
    translate.get('global.confirm.page.deactivate').subscribe((res: string) => {
      this.confirmationText = res;
    });
  }

  canDeactivate(page: CanDeactivatePage): boolean {
    if (page && page.canDeactivatePage && !page.canDeactivatePage()) {
      return confirm(this.confirmationText);
    }
    return true;
  }
}
