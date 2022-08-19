import { Routes } from '@angular/router';

import { DatasetListComponent } from './components/dataset-list/dataset-list.component';
import { DatasetEditionComponent } from './components/dataset-edition/dataset-edition.component';
import { CanDeactivateGuard, AuthGuard } from '@core/guards';
import { Authorization } from '@model';
import { ChutneyLeftMenuComponent } from '@shared/components/chutney-left-menu/chutney-left-menu.component';
import { ChutneyMainHeaderComponent } from '@shared/components/chutney-main-header/chutney-main-header.component';

export const DatasetRoute: Routes = [
    { path: '', component: ChutneyMainHeaderComponent, outlet: 'header' },
    { path: '', component: ChutneyLeftMenuComponent, outlet: 'left-side-bar' },
    {
        path: '',
        component: DatasetListComponent,
        canActivate: [AuthGuard],
        data: { 'authorizations': [ Authorization.DATASET_READ,Authorization.DATASET_WRITE ] }
    },
    {
        path: ':id/edition',
        component: DatasetEditionComponent,
        canDeactivate: [CanDeactivateGuard],
        canActivate: [AuthGuard],
        data: { 'authorizations': [ Authorization.DATASET_WRITE ] }
    },
    {
        path: 'edition',
        component: DatasetEditionComponent,
        canDeactivate: [CanDeactivateGuard],
        canActivate: [AuthGuard],
        data: { 'authorizations': [ Authorization.DATASET_WRITE ] }
    }
];
