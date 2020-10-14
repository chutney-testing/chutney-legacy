import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@env/environment';
import { map } from 'rxjs/operators';
import { Linkifier } from '@model';

@Injectable({
    providedIn: 'root'
})
export class LinkifierService {

    private url = '/api/v1/ui/plugins/linkifier/';

    constructor(private http: HttpClient) {
    }

    public loadLinkifiers(): Observable<Array<Linkifier>> {
        return this.http.get<Array<Linkifier>>(environment.backend + this.url)
            .pipe(
                map(x => LinkifierService.updateSessionStorage(x))
            );
    }

    private static updateSessionStorage(linkifiers: Array<Linkifier>): Array<Linkifier> {
        sessionStorage.setItem('linkifiers', JSON.stringify(linkifiers));
        return linkifiers;
    }

    public add(linkifier: Linkifier): Observable<String> {
        return this.http.post(environment.backend + this.url, linkifier, {responseType: 'text'});
    }

    public remove(linkifier: Linkifier): Observable<String> {
        return this.http.delete(environment.backend + this.url + linkifier.id, {responseType: 'text'});
    }
}
