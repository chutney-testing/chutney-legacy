import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Route } from '@angular/router';
import { LoginComponent } from './containers/login/login.component';
import { LoginFormComponent } from './components/login-form/login-form.component';
import { UiMaterialModule } from '@chutney/ui-material';
import { ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';

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
export class AuthModule {}
