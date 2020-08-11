import { EventManagerService } from '@shared/event-manager.service';

import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { HomePage } from '@model';
import { environment } from '@env/environment';
import { HttpClient } from '@angular/common/http';

@Injectable({
    providedIn: 'root'
})
export class HomePageService {
    private resourceUrl = '/api/homepage/v1';

    private isLoaded = false;

    constructor(private http: HttpClient, private eventManager: EventManagerService) { }


    loadHomePage(): Observable<HomePage> {
        this.isLoaded = true;
        return this.http.get<HomePage>(environment.backend + this.resourceUrl)
            .pipe(map((res: HomePage) => {
                return new HomePage(res.content);
            }))
            .pipe(catchError(this.handleErrorObservable));
    }

    save(homepage: HomePage): Observable<HomePage> {
        return this.http.post<HomePage>(environment.backend + this.resourceUrl, homepage)
            .pipe(map((res: HomePage) => {
                const homePage: HomePage = new HomePage(res.content);
                this.eventManager.broadcast({ name: 'homePageModified', content: homePage });
                return homePage;
            }))
            .pipe(catchError(this.handleErrorObservable));
    }

    private handleErrorObservable(error: Response | any) {
        console.error(error.message || error);
        return throwError(error.message || error);
    }

}
