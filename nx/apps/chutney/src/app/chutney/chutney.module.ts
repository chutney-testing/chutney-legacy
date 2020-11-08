import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { ChutneyComponent } from './chutney.component';
import { RouterModule } from '@angular/router';
import { FeatureAuthModule, authRoutes } from '@chutney/feature-auth';
import { UiLayoutModule } from '@chutney/ui-layout';
import { AuthGuard } from '@chutney/feature-auth';
import { CommonModule } from '@angular/common';
import { AuthLayoutComponent } from '@chutney/ui-layout';
import { MainLayoutComponent } from '@chutney/ui-layout';

@NgModule({
  declarations: [ChutneyComponent],
  imports: [
    CommonModule,
    RouterModule.forChild([
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
        ],
      },
      {
        path: 'auth',
        component: AuthLayoutComponent,
        children: authRoutes,
      },
      { path: '**', redirectTo: 'scenarios' },
    ]),
    UiLayoutModule,
    FeatureAuthModule,
    // GraphQLModule,
  ],
  providers: [],
  exports: [CommonModule],
})
export class ChutneyModule {}
