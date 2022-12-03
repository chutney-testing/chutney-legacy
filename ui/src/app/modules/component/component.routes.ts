import { Routes } from '@angular/router';

import { CreateComponent } from './components/create-component/create-component.component';
import { AuthGuard } from '@core/guards';
import { Authorization } from '@model';

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
