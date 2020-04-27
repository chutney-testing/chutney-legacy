import { Routes } from '@angular/router';
import { ScenariosComponent } from './components/search-list/scenarios.component';
import { ScenarioExecutionComponent } from './components/execution/execution.component';
import { EditionComponent } from './components/edition/scenario-edition/edition.component';
import { RawEditionComponent } from './components/edition/raw/raw-edition.component';
import { ComponentEditionComponent } from './components/edition/component-edition/component-edition.component';
import { ExecuteComponent } from './components/execute/execute.component';
import { CanDeactivateGuard } from '@core/guards';

export const scenarioRoute: Routes = [
    { path: '', component: ScenariosComponent },
    { path: ':id/execution/:execId', component: ScenarioExecutionComponent },
    { path: 'edition', component: EditionComponent, canDeactivate: [CanDeactivateGuard] },
    { path: ':id/edition', component: EditionComponent, canDeactivate: [CanDeactivateGuard] },
    { path: 'raw-edition', component: RawEditionComponent, canDeactivate: [CanDeactivateGuard] },
    { path: ':id/raw-edition', component: RawEditionComponent, canDeactivate: [CanDeactivateGuard] },
    { path: 'component-edition', component: ComponentEditionComponent, canDeactivate: [CanDeactivateGuard] },
    { path: ':id/component-edition', component: ComponentEditionComponent, canDeactivate: [CanDeactivateGuard] },
    { path: ':id/execute/:env', component: ExecuteComponent, canDeactivate: [CanDeactivateGuard] },
];
