import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '@env/environment';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable({
    providedIn: 'root'
})
export class InfoService {

    private url = '/api/v1/info';

    constructor(private http: HttpClient) {
    }

    public getVersion(): Observable<string> {
        return this.http.get(environment.backend + this.url + "/version", {responseType: 'text'});
    }

    public getApplicationName(): Observable<string> {
        return this.http.get(environment.backend + this.url + "/appname", {responseType: 'text'});
    }
}
