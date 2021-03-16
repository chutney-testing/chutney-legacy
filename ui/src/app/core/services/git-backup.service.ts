import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@env/environment';
import { GitRemoteConfig } from '@model';

@Injectable({
    providedIn: 'root'
})
export class GitBackupService {

    private url = '/api/v1/backups/git/';

    constructor(private http: HttpClient) {
    }

    public loadConfig(): Observable<Array<GitRemoteConfig>> {
        return this.http.get<Array<GitRemoteConfig>>(environment.backend + this.url);
    }

    public add(remoteConfig: GitRemoteConfig): Observable<string> {
        return this.http.post<string>(environment.backend + this.url, remoteConfig);
    }

    public remove(remoteConfig: GitRemoteConfig): Observable<string> {
        return this.http.delete<string>(environment.backend + this.url + remoteConfig.name);
    }
}
