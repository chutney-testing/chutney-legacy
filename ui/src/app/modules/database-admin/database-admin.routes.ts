import { Routes } from '@angular/router';
import { DatabaseAdminComponent } from './components/database-admin.component';
import { ChutneyMainHeaderComponent } from '@shared/components/chutney-main-header/chutney-main-header.component';
import { ChutneyLeftMenuComponent } from '@shared/components/chutney-left-menu/chutney-left-menu.component';

export const DatabaseAdminRoute: Routes = [
    {
        path: '',
        component: DatabaseAdminComponent
    }
];
