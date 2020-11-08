import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { ChutneyComponent } from './chutney.component';
import { RouterModule } from '@angular/router';
import { FeatureAuthModule, authRoutes } from '@chutney/feature-auth';
import { UiLayoutModule } from '@chutney/ui-layout';
import { AuthGuard } from '@chutney/feature-auth';
import { CommonModule } from '@angular/common';

@NgModule({
  declarations: [ChutneyComponent],
  imports: [
    CommonModule,
    RouterModule.forChild([/*{
      path: '',
      component: ChutneyComponent,
      children: [*/
        {
          path: '',
          //component: ChutneyComponent,
          pathMatch: 'full', redirectTo: 'scenarios'
        },
        {
          path: 'auth',
          //component: ChutneyComponent,
          children: authRoutes
        },
        {
          path: 'scenarios',
          //component: ChutneyComponent,
          loadChildren: () =>
            import('@chutney/feature-scenarios').then(
              (module) => module.FeatureScenariosModule
            ),
          canActivate: [AuthGuard],
        },
      /*]
    }*/]),
    UiLayoutModule,
    FeatureAuthModule,
    // GraphQLModule,
  ],
  providers: [],
  exports: [
    CommonModule
  ]
})
export class ChutneyModule {
}
