import { Routes } from '@angular/router';

import { EnvironmentAdminComponent } from './environment-admin.component';
import { AuthGuard } from '@core/guards';
import { Authorization } from '@model';

export const environmentAdminRoute: Routes = [
    {
        path: '',
        component: EnvironmentAdminComponent,
        canActivate: [AuthGuard],
        data: { 'authorizations': [ Authorization.ENVIRONMENT_ACCESS,Authorization.ADMIN_ACCESS ] }
    }
];
