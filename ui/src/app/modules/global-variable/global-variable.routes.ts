import { Routes } from '@angular/router';
import { GlobalVariableEditionComponent } from './components/global-variable-edition/global-variable-edition.component';

import { AuthGuard } from '@core/guards';
import { Authorization } from '@model';
import { ChutneyLeftMenuComponent } from '@shared/components/chutney-left-menu/chutney-left-menu.component';
import { ChutneyMainHeaderComponent } from '@shared/components/chutney-main-header/chutney-main-header.component';

export const GlobalVariableRoute: Routes = [
    { path: '', component: ChutneyMainHeaderComponent, outlet: 'header' },
    { path: '', component: ChutneyLeftMenuComponent, outlet: 'left-side-bar' },
    {
        path: '',
        component: GlobalVariableEditionComponent,
        canActivate: [AuthGuard],
        data: { 'authorizations': [ Authorization.GLOBAL_VAR_READ,Authorization.GLOBAL_VAR_WRITE ] }
    }
];
