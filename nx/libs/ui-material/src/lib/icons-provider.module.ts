import { NgModule } from '@angular/core';

import { MatIconModule, MatIconRegistry } from '@angular/material/icon';
import { DomSanitizer } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';

@NgModule({
  imports: [MatIconModule, HttpClientModule],
  exports: [],
})
export class IconsProviderModule {
  constructor(
    private matIconRegistry: MatIconRegistry,
    sanitizer: DomSanitizer
  ) {
    ['spinner', 'pin', 'pin_off', 'restart'].forEach((icon) => {
      matIconRegistry.addSvgIcon(
        icon,
        sanitizer.bypassSecurityTrustResourceUrl(`assets/icons/${icon}.svg`)
      );
    });
  }
}
