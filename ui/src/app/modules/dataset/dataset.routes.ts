import { Routes } from '@angular/router';

import { DatasetListComponent } from './components/dataset-list/dataset-list.component';
import { DatasetEditionComponent } from './components/dataset-edition/dataset-edition.component';
import { CanDeactivateGuard, AuthGuard } from '@core/guards';
import { Authorization } from '@model';

export const DatasetRoute: Routes = [
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
