import { Routes } from '@angular/router';

import { Authorization } from '@model';
import { TargetsComponent } from '@modules/target/list/targets.component';
import { TargetComponent } from '@modules/target/details/target.component';
import { TargetsResolver } from '@modules/target/resolver/targets-resolver.service';
import { EnvironmentsNamesResolver } from '@core/services/environments-names.resolver';
import { EnvironmentsComponent } from '@modules/environment/list/environments.component';
import { EnvironmentsResolver } from '@core/services/environments.resolver';

export const environmentsRoutes: Routes = [
    {
        path: '',
        component: EnvironmentsComponent,
        resolve: {environments: EnvironmentsResolver},
        data: { 'authorizations': [ Authorization.ENVIRONMENT_ACCESS ] }
    }
];
