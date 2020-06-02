import { Routes } from '@angular/router';
import { DatasetListComponent } from './components/dataset-list/dataset-list.component';
import { DatasetEditionComponent } from './components/dataset-edition/dataset-edition.component';
import { CanDeactivateGuard } from '@core/guards';

export const DatasetRoute: Routes = [

    { path: '', component: DatasetListComponent },
    { path: ':id/edition', component: DatasetEditionComponent, canDeactivate: [CanDeactivateGuard]},
    { path: 'edition', component: DatasetEditionComponent, canDeactivate: [CanDeactivateGuard]}
];

