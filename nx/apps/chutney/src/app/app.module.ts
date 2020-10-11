import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppComponent } from './app.component';
import { RouterModule } from '@angular/router';
import { FeatureAuthModule, authRoutes } from '@chutney/feature-auth';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { UiLayoutModule } from '@chutney/ui-layout';
import { AuthGuard } from '@chutney/feature-auth';
import { GraphQLModule } from './graphql.module';

@NgModule({
  declarations: [AppComponent],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    RouterModule.forRoot([
      { path: '', pathMatch: 'full', redirectTo: 'scenarios' },
      { path: 'auth', children: authRoutes },
      {
        path: 'scenarios',
        loadChildren: () =>
          import('@chutney/feature-scenarios').then(
            (module) => module.FeatureScenariosModule
          ),
        canActivate: [AuthGuard],
      },
    ]),
    UiLayoutModule,
    FeatureAuthModule,
    GraphQLModule,
  ],
  providers: [],
  bootstrap: [AppComponent],
})
export class AppModule {}
