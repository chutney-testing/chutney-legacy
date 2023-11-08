import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '@env/environment';
import { map, Observable } from 'rxjs';
import { Metric } from '@core/model/metric.model';

@Injectable({
    providedIn: 'root'
})
export class PrometheusService {

    private url = '/actuator';
    constructor(private http: HttpClient) {
    }

    public getMetrics(): Observable<Metric[]> {
        return this.http.get(environment.backend + this.url + '/prometheus', { responseType: 'text' })
        .pipe(map((res: string) => {
            const metricRegex = new RegExp('(?<name>[^{]*)(?<tags>{.*})? (?<value>.*)');
            return res.split('\n')
            .filter(element => element && !element.startsWith('#'))
            .map(element => {
                const [, name, tags, value] = metricRegex.exec(element) || [];
                return new Metric(name, tags, value);
            });
        }));        
    }
}
