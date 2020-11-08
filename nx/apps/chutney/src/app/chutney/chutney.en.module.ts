/**
 * Should hold only internationalisation related stuff like translations
 *
 * Another good examples of what should be here are locales for MomentJS, Timezones
 *
 * Site module should be imported here
 */
import { ChutneyModule } from './chutney.module';
import { NgModule } from '@angular/core';
import { TRANSLATION } from '@chutney/feature-i18n';
import { en } from '@chutney/feature-i18n';

@NgModule({
  imports: [
    ChutneyModule,
  ],
  providers: [
    // providing the value of english translation data
    {provide: TRANSLATION, useValue: en},
  ],
})
export class ChutneyEnModule {}
