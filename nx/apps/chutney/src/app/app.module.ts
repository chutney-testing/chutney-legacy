import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { RouterModule } from '@angular/router';
import { AppComponent } from './app.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { GraphQLModule } from './graphql.module';
import {
  AuthLayoutComponent,
  MainLayoutComponent,
  UiLayoutModule,
} from '@chutney/ui-layout';

import {
  AuthGuard,
  authRoutes,
  FeatureAuthModule,
} from '@chutney/feature-auth';
import { TranslocoConfigModule } from '@chutney/feature-i18n';

/**
 * Only necessary "globals" and i18n routing of site are imported here
 */
@NgModule({
  declarations: [AppComponent],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    UiLayoutModule,
    GraphQLModule,
    //RouterModule.forRoot(routes, { enableTracing: true } ),
    //RouterModule.forRoot(routes, { enableTracing: true } ),
    TranslocoConfigModule.forRoot(false, ['fr', 'en']),
    RouterModule.forRoot(
      [
        {
          path: '',
          component: MainLayoutComponent,
          canActivate: [AuthGuard],
          children: [
            { path: '', redirectTo: 'scenarios', pathMatch: 'full' },
            {
              path: 'scenarios',
              loadChildren: () =>
                import('@chutney/feature-scenarios').then(
                  (module) => module.FeatureScenariosModule
                ),
            },
            {
              path: 'campaigns',
              loadChildren: () =>
                import('@chutney/feature-campaigns').then(
                  (module) => module.FeatureCampaignsModule
                ),
            },
            {
              path: 'variables',
              loadChildren: () =>
                import('@chutney/feature-variables').then(
                  (module) => module.FeatureVariablesModule
                ),
            },
          ],
        },
        {
          path: 'auth',
          component: AuthLayoutComponent,
          children: authRoutes,
        },
        { path: '**', redirectTo: 'scenarios' },
      ],
      {
        relativeLinkResolution: 'legacy',
        enableTracing: true,
      }
    ),
    UiLayoutModule,
    FeatureAuthModule,
  ],
  bootstrap: [AppComponent],
})
export class AppModule {}
