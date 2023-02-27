import { Routes } from '@angular/router';

import { Authorization } from '@model';
import { TargetsComponent } from '@modules/target/list/targets.component';
import { TargetComponent } from '@modules/target/details/target.component';
import { TargetsResolver } from '@modules/target/resolver/targets-resolver.service';
import { EnvironmentsNamesResolver } from '@core/services/environments-names.resolver';

export const targetsRoutes: Routes = [
    {
        path: '',
        component: TargetsComponent,
        data: { 'authorizations': [ Authorization.ENVIRONMENT_ACCESS ] }
    },
    {
        path: ':name',
        component: TargetComponent,
        resolve: {targets: TargetsResolver, environmentsNames: EnvironmentsNamesResolver},
        data: { 'authorizations': [ Authorization.ADMIN_ACCESS ] }
    }
];
