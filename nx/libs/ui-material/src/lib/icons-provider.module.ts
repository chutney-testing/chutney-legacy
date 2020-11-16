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
    matIconRegistry.addSvgIcon(
      'spinner',
      sanitizer.bypassSecurityTrustResourceUrl('assets/icons/spinner.svg')
    );
  }
}
