import { Routes } from '@angular/router';
import { DatasetListComponent } from './components/dataset-list/dataset-list.component';
import { DatasetEditionComponent } from './components/dataset-edition/dataset-edition.component';

export const DatasetRoute: Routes = [

    { path: '', component: DatasetListComponent },
    { path: ':id/edition', component: DatasetEditionComponent},
    { path: 'edition', component: DatasetEditionComponent},
];

