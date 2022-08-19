import { Routes } from '@angular/router';
import { PluginConfigurationComponent } from './components/plugin-configuration.component';
import { ChutneyMainHeaderComponent } from '@shared/components/chutney-main-header/chutney-main-header.component';
import { ChutneyLeftMenuComponent } from '@shared/components/chutney-left-menu/chutney-left-menu.component';

export const PluginConfigurationRoute: Routes = [
    { path: '', component: ChutneyMainHeaderComponent, outlet: 'header' },
    { path: '', component: ChutneyLeftMenuComponent, outlet: 'left-side-bar' },
    {
        path: '',
        component: PluginConfigurationComponent
    }
];
