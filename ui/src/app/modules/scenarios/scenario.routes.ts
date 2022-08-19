import { Routes } from '@angular/router';
import { ScenariosComponent } from './components/search-list/scenarios.component';
import { ScenarioExecutionComponent } from './components/execution/execution.component';
import { RawEditionComponent } from './components/edition/raw/raw-edition.component';
import { ComponentEditionComponent } from './components/edition/component-edition/component-edition.component';
import { ExecuteComponent } from './components/execute/execute.component';
import { CanDeactivateGuard, AuthGuard } from '@core/guards';
import { Authorization } from '@model';
import { ChutneyMainHeaderComponent } from '@shared/components/chutney-main-header/chutney-main-header.component';
import { ChutneyLeftMenuComponent } from '@shared/components/chutney-left-menu/chutney-left-menu.component';

export const scenarioRoute: Routes = [
    { path: '', component: ChutneyMainHeaderComponent, outlet: 'header' },
    { path: '', component: ChutneyLeftMenuComponent, outlet: 'left-side-bar' },
    {
        path: '',
        component: ScenariosComponent,
        canActivate: [AuthGuard],
        data: { 'authorizations': [ Authorization.SCENARIO_READ ] }
    },
    {
        path: ':id/execution/:execId',
        component: ScenarioExecutionComponent,
        canActivate: [AuthGuard],
        data: { 'authorizations': [ Authorization.SCENARIO_READ ] }
    },
    {
        path: 'raw-edition',
        component: RawEditionComponent,
        canDeactivate: [CanDeactivateGuard],
        canActivate: [AuthGuard],
        data: { 'authorizations': [ Authorization.SCENARIO_WRITE ] }
    },
    {
        path: ':id/raw-edition',
        component: RawEditionComponent,
        canDeactivate: [CanDeactivateGuard],
        canActivate: [AuthGuard],
        data: { 'authorizations': [ Authorization.SCENARIO_WRITE ] }
    },
    {
        path: 'component-edition',
        component: ComponentEditionComponent,
        canDeactivate: [CanDeactivateGuard],
        canActivate: [AuthGuard],
        data: { 'authorizations': [ Authorization.SCENARIO_WRITE ] }
    },
    {
        path: ':id/component-edition',
        component: ComponentEditionComponent,
        canDeactivate: [CanDeactivateGuard],
        canActivate: [AuthGuard],
        data: { 'authorizations': [ Authorization.SCENARIO_WRITE ] }
    },
    {
        path: ':id/execute/:env',
        component: ExecuteComponent,
        canDeactivate: [CanDeactivateGuard],
        canActivate: [AuthGuard],
        data: { 'authorizations': [ Authorization.SCENARIO_EXECUTE ] }
    },
];
