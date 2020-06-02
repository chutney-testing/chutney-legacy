import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { environment } from '@env/environment';
import { Backup } from '@core/model/backups.model';

@Injectable({
    providedIn: 'root'
})
export class BackupsService {

    private url = '/api/v1/backups';

    constructor(private http: HttpClient) {
    }

    public list(): Observable<Array<Backup>> {
        return this.http.get<Array<BackupDto>>(environment.backend + this.url).pipe(
            map(dtos => this.mapToBackups(dtos))
        );
    }

    public get(backup: Backup): Observable<Backup> {
        return this.http.get<BackupDto>(
            environment.backend + this.url + `/${backup.id()}`).pipe(
            map(dto => this.mapToBackup(dto))
        );
    }

    public delete(backup: Backup): Observable<void> {
        return this.http.delete(environment.backend + this.url + `/${backup.id()}`)
            .pipe(map(() => {}));
    }

    public download(backup: Backup): Observable<any> {
        const options: any = {
            responseType: 'arraybuffer'
        };
        return this.http.get(environment.backend + this.url + `/${backup.id()}` + '/download', options);
    }

    public save(backup: BackupDto): Observable<String> {
        return this.http.post(environment.backend + this.url, backup, {responseType: 'text'});
    }

    private mapToBackup(dto: BackupDto): Backup {
        return new Backup(dto.homePage, dto.agentsNetwork, dto.environments, dto.components, dto.globalVars, dto.time);
    }

    private mapToBackups(dtos: BackupDto[]): Backup[] {
        return dtos.map(dto => this.mapToBackup(dto));
    }
}

export class BackupDto {
    constructor(
        public homePage: boolean,
        public agentsNetwork: boolean,
        public environments: boolean,
        public components: boolean,
        public globalVars: boolean,
        public time?: Date) {
    }
}
