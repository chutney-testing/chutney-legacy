import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '@env/environment';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable({
    providedIn: 'root'
})
export class GlobalVariableService {

    private url = '/api/ui/globalvar/v1/';

    constructor(private http: HttpClient) {
    }

    public get(fileName: string): Observable<string> {
        return this.http.get<Object>(environment.backend + this.url + fileName).pipe(map((res: Object) => {
            return res['message'];
        }));
    }

    public save(fileName: string, content: string): Observable<Object> {
        return this.http.post(environment.backend + this.url + fileName, {message: content}, {responseType: 'text'});
    }

    public list() {
        return this.http.get<Object>(environment.backend + this.url).pipe(map((res: Object) => {
            return res;
        }));
    }

    delete(fileName: string) {
        return this.http.delete<Object>(environment.backend + this.url + fileName);
    }
}
