import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { RouterModule, Routes } from '@angular/router';
import { AppComponent } from './app.component';
import { ChutneyAppLanguage } from '@chutney/feature-i18n';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { GraphQLModule } from './chutney/graphql.module';
import { UiLayoutModule } from '@chutney/ui-layout';

/**
 * Specific i18n site routes prefixed by i18n variation
 */
const routes: Routes = [
  {path: ChutneyAppLanguage.English, loadChildren: () =>
      import('./chutney/chutney.en.module').then(
        (module) => module.ChutneyEnModule
      )}, // lazy loading the English site module
  {path: ChutneyAppLanguage.French, loadChildren: () =>
      import('./chutney/chutney.fr.module').then(
        (module) => module.ChutneyFrModule
      )},   // lazy loading the Czech site module

  {path: '**', redirectTo: ChutneyAppLanguage.French},  // redirecting to default route in case of any other prefix
];


/**
 * Only necessary "globals" and i18n routing of site are imported here
 */
@NgModule({
  declarations: [
    AppComponent,
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    UiLayoutModule,
    GraphQLModule,
    RouterModule.forRoot(routes),
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule {}
