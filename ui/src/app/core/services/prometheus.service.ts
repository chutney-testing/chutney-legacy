import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '@env/environment';
import { map, Observable } from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class PrometheusService {

    private url = '/actuator';
    constructor(private http: HttpClient) {
    }

    public getMetrics(): Observable<string> {
        return this.http.get(environment.backend + this.url + '/prometheus', { responseType: 'text' });
    }
}
