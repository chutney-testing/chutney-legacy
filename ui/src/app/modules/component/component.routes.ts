import { Routes } from '@angular/router';
import { CreateComponent } from './components/create-component/create-component.component';

export const componentRoute: Routes = [
        { path: '', component: CreateComponent },
        { path: ':id', component: CreateComponent },
];
