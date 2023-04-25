import { Routes } from '@angular/router';
import { ScenariosComponent } from './components/search-list/scenarios.component';
import { RawEditionComponent } from './components/edition/raw/raw-edition.component';
import { ComponentEditionComponent } from './components/edition/component-edition/component-edition.component';
import { ExecuteComponent } from './components/execute/execute.component';
import { AuthGuard, CanDeactivateGuard } from '@core/guards';
import { Authorization } from '@model';
import {
    ScenarioExecutionsHistoryComponent
} from '@modules/scenarios/components/execution/history/scenario-executions-history.component';
import {
    ScenarioExecutionMenuComponent
} from '@modules/scenarios/components/execution/sub/right-side-bar/scenario-execution-menu.component';
import { FeaturesGuard } from '@core/guards/features.guard';
import { FeatureName } from '@core/feature/feature.model';

export const scenarioRoute: Routes = [

    {
        path: '',
        component: ScenariosComponent,
        canActivate: [AuthGuard],
        data: {'authorizations': [Authorization.SCENARIO_READ]}
    },
    {
        path: ':id/executions',
        canActivate: [AuthGuard],
        data: {'authorizations': [Authorization.SCENARIO_READ]},
        children: [
            {
                path: '',
                component: ScenarioExecutionsHistoryComponent
            },
            {
                path: '',
                component: ScenarioExecutionMenuComponent,
                outlet: 'right-side-bar'
            },
        ]
    },
    {
        path: ':id/raw-edition',
        component: RawEditionComponent,
        canDeactivate: [CanDeactivateGuard],
        canActivate: [AuthGuard],
        data: {'authorizations': [Authorization.SCENARIO_WRITE]}
    },
    {
        path: ':id/component-edition',
        component: ComponentEditionComponent,
        canDeactivate: [CanDeactivateGuard],
        canActivate: [AuthGuard, FeaturesGuard],
        data: {
            'authorizations': [Authorization.SCENARIO_WRITE],
            'feature': FeatureName.COMPONENT
        }
    },
    {
        path: ':id/execute/:env',
        component: ExecuteComponent,
        canDeactivate: [CanDeactivateGuard],
        canActivate: [AuthGuard],
        data: {'authorizations': [Authorization.SCENARIO_EXECUTE]}
    },
    {
        path: 'raw-edition',
        component: RawEditionComponent,
        canDeactivate: [CanDeactivateGuard],
        canActivate: [AuthGuard],
        data: {'authorizations': [Authorization.SCENARIO_WRITE]}
    },
    {
        path: 'component-edition',
        component: ComponentEditionComponent,
        canDeactivate: [CanDeactivateGuard],
        canActivate: [AuthGuard, FeaturesGuard],
        data: {'authorizations': [Authorization.SCENARIO_WRITE], 'feature': FeatureName.COMPONENT}
    }
];
