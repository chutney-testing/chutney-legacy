import { Component, OnDestroy, } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { registerLocaleData } from '@angular/common';
import localeFr from '@angular/common/locales/fr';
import { LinkifierService, LoginService } from '@core/services';
import { Subscription } from 'rxjs';

@Component({
  selector: 'chutney-main',
  templateUrl: './app.component.html',
    styleUrls: ['./app.component.scss']
})
export class AppComponent{



  constructor(private translate: TranslateService) {
    // this language will be used as a fallback when a translation isn't found in the current language
    translate.setDefaultLang('en');
    // // the lang to use, if the lang isn't available, it will use the current loader to get them
    // // take only language designator, i.e. forget about region
    translate.use(navigator.language.substring(0, 2) || translate.getDefaultLang());

    registerLocaleData(localeFr);
  }
}
