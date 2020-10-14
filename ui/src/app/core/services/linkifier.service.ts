import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@env/environment';
import { map } from 'rxjs/operators';

export class Linkifier {
    constructor(public pattern: string,
                public link: string,
                public id?: string) {
        if (this.id === undefined) {
            this.id = this.hash(pattern) + this.hash(link);
        }
    }

    hash(s: string) {
        for (var i = 0, h = 0; i < s.length; i++)
            h = Math.imul(31, h) + s.charCodeAt(i) | 0;
        return h.toString();
    }
}

@Injectable({
    providedIn: 'root'
})
export class LinkifierService {

    private url = '/api/ui/plugins/linkifier/v1/';

    constructor(private http: HttpClient) {
    }

    public get(): Observable<Array<Linkifier>> {
        return this.http.get<Array<Linkifier>>(environment.backend + this.url)
            .pipe(
                map(x => LinkifierService.updateSessionStorage(x))
            );
    }

    private static updateSessionStorage(linkifiers: Array<Linkifier>): Array<Linkifier> {
        sessionStorage.setItem('linkifiers', JSON.stringify(linkifiers));
        return linkifiers;
    }

    public save(linkifier: Linkifier): Observable<String> {
        return this.http.post(environment.backend + this.url, linkifier, {responseType: 'text'});
    }

    public delete(linkifier: Linkifier): Observable<String> {
        return this.http.delete(environment.backend + this.url + linkifier.id, {responseType: 'text'});
    }
}
