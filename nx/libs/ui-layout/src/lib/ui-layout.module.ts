import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MainLayoutComponent } from './containers/main-layout/main-layout.component';
import { UiMaterialModule } from '@chutney/ui-material';
import { RouterModule } from '@angular/router';
import { AppBannerDirective } from './directives/appBanner/app-banner.directive';
import { AuthLayoutComponent } from './containers/auth-layout/auth-layout.component';
import { HeaderComponent } from './components/header/header.component';
import { SidebarComponent } from './components/sidebar/sidebar.component';
import { SidemenuComponent } from './components/sidemenu/sidemenu.component';
import { UserPanelComponent } from './components/user-panel/user-panel.component';

@NgModule({
  imports: [CommonModule, UiMaterialModule, RouterModule],
  declarations: [MainLayoutComponent, AppBannerDirective, AuthLayoutComponent, HeaderComponent, SidebarComponent, SidemenuComponent, UserPanelComponent],
  exports: [MainLayoutComponent, AuthLayoutComponent],
})
export class UiLayoutModule {}
