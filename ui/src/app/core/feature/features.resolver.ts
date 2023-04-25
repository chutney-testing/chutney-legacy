import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Resolve, RouterStateSnapshot } from '@angular/router';
import { Observable } from 'rxjs';
import { Feature } from '@core/feature/feature.model';
import { FeatureService } from '@core/feature/feature.service';

@Injectable({
    providedIn: 'root'
})
export class FeaturesResolver implements Resolve<Feature[]> {

    constructor(private featureService: FeatureService) {
    }

    resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<Feature[]> {
        return this.featureService.loadFeatures();
    }
}
