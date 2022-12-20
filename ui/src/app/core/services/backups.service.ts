import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '@env/environment';
import { Backup } from '@core/model/backups.model';

@Injectable({
    providedIn: 'root'
})
export class BackupsService {

    private url = '/api/v1/backups';

    constructor(private http: HttpClient) {
    }

    list(): Observable<Array<Backup>> {
        return this.http.get<Array<Backup>>(environment.backend + this.url);
    }

    get(id: string): Observable<Backup> {
        return this.http.get<Backup>(
            environment.backend + this.url + `/${id}`);
    }

    delete(id: string): Observable<void> {
        return this.http.delete<void>(environment.backend + this.url + `/${id}`);
    }

    download(id: string): Observable<any> {
        const options: any = {
            responseType: 'arraybuffer'
        };
        return this.http.get(environment.backend + this.url + `/${id}` + '/download', options);
    }

    save(backup: Backup): Observable<String> {
        return this.http.post(environment.backend + this.url, backup, {responseType: 'text'});
    }

    getBackupables(): Observable<string[]> {
        return this.http.get<string[]>(environment.backend + this.url + '/backupables');
    }
}
