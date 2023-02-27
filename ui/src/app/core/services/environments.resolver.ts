import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Resolve, RouterStateSnapshot } from '@angular/router';
import { Observable } from 'rxjs';
import { EnvironmentService } from '@core/services';
import { Environment } from '@model';

@Injectable()
export class EnvironmentsResolver implements Resolve<Environment[]> {

    constructor(private environmentService: EnvironmentService) {
    }

    resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<Environment[]> {
        return this.environmentService.list();
    }
}
