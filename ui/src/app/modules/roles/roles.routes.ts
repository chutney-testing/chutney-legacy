import { Routes } from '@angular/router';
import { RolesComponent } from './components/roles.component';
import { ChutneyMainHeaderComponent } from '@shared/components/chutney-main-header/chutney-main-header.component';
import { ChutneyLeftMenuComponent } from '@shared/components/chutney-left-menu/chutney-left-menu.component';

export const RolesRoute: Routes = [
    { path: '', component: ChutneyMainHeaderComponent, outlet: 'header' },
    { path: '', component: ChutneyLeftMenuComponent, outlet: 'left-side-bar' },
    {
        path: '',
        component: RolesComponent
    }
];
