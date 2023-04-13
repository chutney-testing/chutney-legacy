import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { Feature, FeatureName } from '@core/feature/feature.model';
import { environment } from '@env/environment';
import { tap } from 'rxjs/operators';

@Injectable({
    providedIn: 'root'
})
export class FeatureService {

    private readonly featuresApi = '/api/v2/features';

    private features$:BehaviorSubject<Feature[]> = new BehaviorSubject([]);

    constructor(private http: HttpClient) {
    }

    active(featuresName: FeatureName): boolean {
        if(!featuresName) {
            return true;
        }
        const activeFeatures = this.features$.value.filter(feature => feature.active).map(feature => feature.name);
        return  activeFeatures.includes(featuresName);
    }

    loadFeatures(): Observable<Feature[]> {
        return this.http.get<Feature[]>(environment.backend + this.featuresApi)
            .pipe(
                tap(features => this.features$.next(features))
            );
    }
}
