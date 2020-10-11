import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LayoutComponent } from './containers/layout/layout.component';
import { UiMaterialModule } from '@chutney/ui-material';
import { RouterModule } from '@angular/router';
import { AppBannerDirective } from './directives/appBanner/app-banner.directive';

@NgModule({
  imports: [CommonModule, UiMaterialModule, RouterModule],
  declarations: [LayoutComponent, AppBannerDirective],
  exports: [LayoutComponent],
})
export class UiLayoutModule {}
