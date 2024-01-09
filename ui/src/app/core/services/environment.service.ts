/**
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Environment, EnvironmentVariable, Target, TargetFilter } from '@model';
import { environment as server } from '../../../environments/environment';
import { HttpClient, HttpContext, HttpHeaders, HttpParams } from '@angular/common/http';
import { map, tap } from 'rxjs/operators';
import { saveAs } from 'file-saver';
import { FileSaverService } from 'ngx-filesaver';

@Injectable({
    providedIn: 'root'
})
export class EnvironmentService {

    private envBaseUrl = '/api/v2/environments';
    private targetBaseUrl = '/api/v2/targets';
    private variablesBaseUrl = '/api/v2/variables';

    constructor(private http: HttpClient,
                private fileSaverService: FileSaverService) {
    }

    list(): Observable<Environment[]> {
        return this.http
            .get<Environment[]>(server.backend + this.envBaseUrl)
            .pipe(
                map(res => res.sort((t1, t2) => t1.name.toUpperCase() > t2.name.toUpperCase() ? 1 : 0))
            );
    }

    names(): Observable<Array<string>> {
        return this.http.get<Array<string>>(server.backend + this.envBaseUrl + '/names');
    }

    get(environmentName: string): Observable<Environment> {
        return this.http.get<Environment>(server.backend + this.envBaseUrl + '/' + environmentName);
    }

    export(environmentName: string): Observable<Environment> {
        const fileName = `env.${environmentName}.chutney.json`;
        return this.get(environmentName).pipe(
            tap(env => {
                this.fileSaverService.saveText(JSON.stringify(env), fileName)
            }),
        );
    }

    import(file: File): Observable<Environment> {
        const formData = new FormData();
        formData.append('file', file);
        return this.http.post<Environment>(server.backend + this.envBaseUrl, formData);
    }

    create(environment: Environment): Observable<Object> {
        return this.http.post(server.backend + this.envBaseUrl, environment);
    }

    delete(environmentName: string): Observable<Object> {
        return this.http.delete(server.backend + this.envBaseUrl + '/' + environmentName);
    }

    update(environmentName: string, environment: Environment): Observable<Object> {
        return this.http.put(server.backend + this.envBaseUrl + '/' + environmentName, environment);
    }


    environmentTarget(environmentName: string, targetName: string): Observable<Target> {
        return this.http.get<Target>(server.backend + this.envBaseUrl + '/' + environmentName + '/targets/' + targetName);
    }

    getTargets(filters: TargetFilter): Observable<Target[]> {
        const params = Object.entries(filters)
            .filter(([key, value]) => value != null)
            .reduce((params, [key, value]) => params.append(key, value), new HttpParams());
        return this.http.get<Target[]>(server.backend + this.targetBaseUrl, {params});
    }

    updateTarget(targetName: string, target: Target): Observable<Object> {
        return this.http.put(server.backend + this.targetBaseUrl + '/' + targetName, target);
    }

    addTarget(target: Target): Observable<Object> {
        return this.http.post(server.backend + this.targetBaseUrl, target);
    }

    deleteTarget(targetName: string): Observable<void> {
        return this.http.delete<void>(server.backend + this.targetBaseUrl + '/' + targetName);
    }

    deleteEnvironmentTarget(environmentName: string, targetName: string): Observable<Object> {
        return this.http.delete(server.backend + this.envBaseUrl + '/' + environmentName + '/targets/' + targetName);
    }

    exportTargetOn(environmentName: string, targetName: string): Observable<void> {
        return this.environmentTarget(environmentName, targetName)
            .pipe(
                map(target => {
                    delete target.environment;
                    const fileName = `${environmentName}-${targetName}.chutney.json`;
                    this.fileSaverService.saveText(JSON.stringify(target), fileName);
                    return;
                })
            );
    }

    importTarget(file: File, environment: string): Observable<Target> {
        const formData = new FormData();
        formData.append('file', file);
        return this.http.post<Target>(server.backend + this.envBaseUrl + '/' + environment + '/targets/', formData);
    }


    addVariable(variables: EnvironmentVariable[]): Observable<void> {
        return this.http.post<void>(server.backend + this.variablesBaseUrl, variables);
    }
    updateVariable(key: string, values: EnvironmentVariable[]): Observable<void> {
        return this.http.put<void>(server.backend + this.variablesBaseUrl + '/' + key, values);
    }

    deleteVariable(key: string): Observable<void> {
        return this.http.delete<void>(server.backend + this.variablesBaseUrl + '/' + key);
    }

}
