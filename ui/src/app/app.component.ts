import { Component, } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { registerLocaleData } from '@angular/common';
import localeFr from '@angular/common/locales/fr';

@Component({
  selector: 'chutney-main',
  template: `
    <router-outlet></router-outlet>
`
})
export class AppComponent {

  constructor(translate: TranslateService) {
    // this language will be used as a fallback when a translation isn't found in the current language
    translate.setDefaultLang('en');
    // // the lang to use, if the lang isn't available, it will use the current loader to get them
    // // take only language designator, i.e. forget about region
    translate.use(navigator.language.substring(0, 2) || translate.getDefaultLang());

    registerLocaleData(localeFr);
  }
}
