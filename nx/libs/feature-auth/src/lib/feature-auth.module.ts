import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Route, RouterModule } from '@angular/router';
import { HttpClientModule } from '@angular/common/http';
import { ReactiveFormsModule } from '@angular/forms';
import { LoginComponent } from './containers/login/login.component';
import { LoginFormComponent } from './components/login-form/login-form.component';
import { UiMaterialModule } from '@chutney/ui-material';

export const authRoutes: Route[] = [
  {
    path: 'login',
    component: LoginComponent,
    data: {
      fullscreen: true,
      auth: false,
    },
  },
];

@NgModule({
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    HttpClientModule,
    UiMaterialModule,
  ],
  declarations: [LoginComponent, LoginFormComponent],
})
export class FeatureAuthModule {}
