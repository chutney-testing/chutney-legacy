import { Routes } from '@angular/router';
import { CreateComponent } from './components/create-component/create-component.component';

export const componentRoute: Routes = [
    { path: '', pathMatch: 'full', redirectTo: 'list' },
    { path: ':id', component: CreateComponent }
];
