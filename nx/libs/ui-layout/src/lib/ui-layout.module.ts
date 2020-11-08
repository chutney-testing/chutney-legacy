import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MainLayoutComponent } from './containers/main-layout/main-layout.component';
import { UiMaterialModule } from '@chutney/ui-material';
import { RouterModule } from '@angular/router';
import { AppBannerDirective } from './directives/appBanner/app-banner.directive';
import { AuthLayoutComponent } from './containers/auth-layout/auth-layout.component';

@NgModule({
  imports: [CommonModule, UiMaterialModule, RouterModule],
  declarations: [MainLayoutComponent, AppBannerDirective, AuthLayoutComponent],
  exports: [MainLayoutComponent, AuthLayoutComponent],
})
export class UiLayoutModule {}
