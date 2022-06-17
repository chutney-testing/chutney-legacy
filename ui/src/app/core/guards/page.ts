import { HostListener, Injectable } from '@angular/core';

@Injectable()
export abstract class CanDeactivatePage {
  abstract canDeactivatePage(): boolean;

  @HostListener('window:beforeunload', ['$event'])
  unloadNotification($event: any) {
    if (!this.canDeactivatePage()) {
      $event.returnValue = true;
    }
  }
}
