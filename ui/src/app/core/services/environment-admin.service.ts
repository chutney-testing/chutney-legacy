import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { EnvironmentMetadata, Target } from '@model';
import { environment as server } from '../../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { map } from 'rxjs/operators';

@Injectable({
    providedIn: 'root'
})
export class EnvironmentAdminService {

    private baseUrl = '/api/v2/environment';

    constructor(private http: HttpClient) {}

    listEnvironments(): Observable<Array<EnvironmentMetadata>> {
        return this.http.get<Array<EnvironmentMetadata>>(server.backend + this.baseUrl).pipe(map((res: Array<EnvironmentMetadata>) => {
            return res.sort((t1, t2) => t1.name.toUpperCase() > t2.name.toUpperCase() ? 1 : 0);
        }));
    }

    exportEnvironment(environmentName: string): Observable<Array<Target>> {
        return this.http.get<Array<Target>>(server.backend + this.baseUrl + '/' + environmentName);
    }

    createEnvironment(environment: EnvironmentMetadata): Observable<Object> {
        return this.http.post(server.backend + this.baseUrl, environment);
    }

    updateEnvironment(environmentName: string, environment: EnvironmentMetadata): Observable<Object> {
        return this.http.put(server.backend + this.baseUrl + '/' + environmentName, environment);
    }

    listTargets(environmentName: string): Observable<Array<Target>> {
        return this.http.get<Array<Target>>(server.backend + this.baseUrl + '/' + environmentName + '/target');
    }

    targets(): Observable<Array<Target>> {
        return this.http.get<Array<Target>>(server.backend + this.baseUrl + '/target');
    }

    updateTarget(environmentName: string, targetName: string, target: Target): Observable<Object> {
        return this.http.put(server.backend + this.baseUrl + '/' + environmentName + '/target/' + targetName, target);
    }

    addTarget(environmentName: string, target: Target): Observable<Object> {
        return this.http.post(server.backend + this.baseUrl + '/' + environmentName + '/target', target);
    }

    deleteTarget(environmentName: string, targetName: string): Observable<Object> {
        return this.http.delete(server.backend + this.baseUrl + '/' + environmentName + '/target/' + targetName);
    }

    exportTarget(environmentName: string, targetName: string): Observable<Object> {
        return this.http.get(server.backend + this.baseUrl + '/' + environmentName + '/target/' + targetName);
    }

}
