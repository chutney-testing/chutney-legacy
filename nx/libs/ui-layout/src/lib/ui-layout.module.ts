import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MainLayoutComponent } from './containers/main-layout/main-layout.component';
import { UiMaterialModule } from '@chutney/ui-material';
import { RouterModule } from '@angular/router';
import { AppBannerDirective } from './directives/appBanner/app-banner.directive';
import { AuthLayoutComponent } from './containers/auth-layout/auth-layout.component';
import { HeaderComponent } from './components/header/header.component';
import { SidebarComponent } from './components/sidebar/sidebar.component';
import { UserPanelComponent } from './components/user-panel/user-panel.component';
import { AccordionDirective } from './directives/accordion/accordion.directive';
import { AccordionItemDirective } from './directives/accordion/accordion-item.directive';
import { AccordionToggleDirective } from './directives/accordion/accordion-toggle.directive';
import { TranslationComponent } from './components/translation/translation.component';
import { UserMenuComponent } from './components/user-menu/user-menu.component';
import { AvatarModule } from 'ngx-avatar';
import { SidemenuComponent } from './components/sidemenu/sidemenu.component';
import { TranslocoConfigModule } from '@chutney/feature-i18n';
import { TranslocoModule } from '@ngneat/transloco';

const loader = ['en', 'fr'].reduce((acc: any, lang: string) => {
  acc[lang] = () => import(`./i18n/${lang}.json`);
  return acc;
}, {});

@NgModule({
  imports: [
    CommonModule,
    UiMaterialModule,
    RouterModule,
    AvatarModule,
    TranslocoModule,
  ],
  declarations: [
    MainLayoutComponent,
    AppBannerDirective,
    AuthLayoutComponent,
    HeaderComponent,
    SidebarComponent,
    UserPanelComponent,
    AccordionDirective,
    AccordionItemDirective,
    AccordionToggleDirective,
    TranslationComponent,
    UserMenuComponent,
    SidemenuComponent,
  ],
  exports: [MainLayoutComponent, AuthLayoutComponent],
})
export class UiLayoutModule {}
