import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '@env/environment';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable({
    providedIn: 'root'
})
export class PluginManagerService {

    private url = '/api/ui/plugins/v1/';

    constructor(private http: HttpClient) {
    }

    public getDefinition(pluginId: string) {
        return this.http.get<Object>(environment.backend + this.url + pluginId).pipe(map((res: Object) => {
            return res;
        }));
    }

    public getDefinitions(page: string, section: string) {
        let params = new HttpParams();
        params = params.append('page', page);
        params = params.append('section', section);

        return this.http.get<Object>(environment.backend + this.url, { params: params }).pipe(map((res: Object) => {
            return res;
        }));
    }

    public listPlugins(page: string, section: string) {
        return this.http.get<Object>(environment.backend + this.url).pipe(map((res: Object) => {
            return res;
        }));
    }

}
