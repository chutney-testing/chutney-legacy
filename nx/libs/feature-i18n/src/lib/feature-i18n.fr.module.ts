import { NgModule } from '@angular/core';
import { fr } from './i18n/fr.translation';
import { TRANSLATION } from './i18n/utils';

import { FeatureI18nModule } from './feature-i18n.module';

/**
 * Should hold only internationalisation related stuff like translations
 *
 * Another good examples of what should be here are locales for MomentJS, Timezones
 *
 * Site module should be imported here
 */
@NgModule({
  imports: [
    FeatureI18nModule,
  ],
  providers: [
    // providing the value of french translation data
    {provide: TRANSLATION, useValue: fr},
  ],
})
export class FeatureI18nFrModule {}
