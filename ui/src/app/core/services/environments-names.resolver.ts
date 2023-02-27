import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Resolve, RouterStateSnapshot } from '@angular/router';
import { Observable } from 'rxjs';
import { EnvironmentService } from '@core/services';

@Injectable()
export class EnvironmentsNamesResolver implements Resolve<string[]> {

    constructor(private environmentService: EnvironmentService) {
    }

    resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<string[]> {
        return this.environmentService.names();
    }
}
