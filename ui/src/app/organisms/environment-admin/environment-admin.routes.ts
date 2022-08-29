import { Routes } from '@angular/router';

import { EnvironmentAdminComponent } from './environment-admin.component';
import { AuthGuard } from '@core/guards';
import { Authorization } from '@model';
import { ChutneyMainHeaderComponent } from '@shared/components/chutney-main-header/chutney-main-header.component';
import { ChutneyLeftMenuComponent } from '@shared/components/chutney-left-menu/chutney-left-menu.component';

export const environmentAdminRoute: Routes = [
    { path: '', component: ChutneyMainHeaderComponent, outlet: 'header' },
    { path: '', component: ChutneyLeftMenuComponent, outlet: 'left-side-bar' },
    {
        path: '',
        component: EnvironmentAdminComponent,
        canActivate: [AuthGuard],
        data: { 'authorizations': [ Authorization.ENVIRONMENT_ACCESS,Authorization.ADMIN_ACCESS ] }
    }
];
