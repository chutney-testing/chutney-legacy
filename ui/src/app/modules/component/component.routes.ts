import { Routes } from '@angular/router';

import { CreateComponent } from './components/create-component/create-component.component';
import { AuthGuard } from '@core/guards';
import { Authorization } from '@model';
import { ChutneyLeftMenuComponent } from '@shared/components/chutney-left-menu/chutney-left-menu.component';
import { ChutneyRightMenuComponent } from '@shared/components/chutney-right-menu/chutney-right-menu.component';
import { ChutneyMainHeaderComponent } from '@shared/components/chutney-main-header/chutney-main-header.component';

export const componentRoute: Routes = [
    {
        path: '',
        pathMatch: 'full',
        redirectTo: 'list',
        data: {'authorizations': [Authorization.COMPONENT_READ, Authorization.COMPONENT_WRITE]}
    },
    {
        path: ':id',
        component: CreateComponent,
        canActivate: [AuthGuard],
        data: {'authorizations': [Authorization.COMPONENT_READ, Authorization.COMPONENT_WRITE]}
    }
];
