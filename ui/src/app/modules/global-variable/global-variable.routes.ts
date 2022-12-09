import { Routes } from '@angular/router';
import { GlobalVariableEditionComponent } from './components/global-variable-edition/global-variable-edition.component';

import { AuthGuard } from '@core/guards';
import { Authorization } from '@model';

export const GlobalVariableRoute: Routes = [
    {
        path: '',
        component: GlobalVariableEditionComponent,
        canActivate: [AuthGuard],
        data: { 'authorizations': [ Authorization.GLOBAL_VAR_READ,Authorization.GLOBAL_VAR_WRITE ] }
    }
];
