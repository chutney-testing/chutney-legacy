import { Injectable } from '@angular/core';
import {
    Router, Resolve,
    RouterStateSnapshot,
    ActivatedRouteSnapshot
} from '@angular/router';
import { Observable, of } from 'rxjs';
import { Target, TargetFilter } from '@model';
import { EnvironmentService } from '@core/services';

@Injectable()
export class TargetsResolver implements Resolve<Target[]> {

    constructor(private environmentService: EnvironmentService) {
    }

    resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<Target[]> {
        const name = route.params['name'];
        return name === 'new' ? of([]) : this.environmentService.getTargets(new TargetFilter(name, null));
    }
}
