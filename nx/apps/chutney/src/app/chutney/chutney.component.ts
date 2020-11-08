import { Component, Inject } from '@angular/core';
import {
  ChutneyAppLanguage,
  Translation,
  TRANSLATION,
} from '@chutney/feature-i18n';

@Component({
  selector: 'chutney-root',
  templateUrl: './chutney.component.html',
  styleUrls: ['./chutney.component.scss'],
})
export class ChutneyComponent {
  title = 'chutney';
  // this will extract tuple of translation key and name from enum
  translations = Object.entries(ChutneyAppLanguage);

  constructor(@Inject(TRANSLATION) public readonly lang: Translation) {
    // just a simple log to demonstrate usage in component class
    console.log('current language is', lang.language);
  }
}
