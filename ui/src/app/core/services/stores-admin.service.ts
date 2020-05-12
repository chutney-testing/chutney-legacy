import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '@env/environment';
import { GitRepository } from '@model';
import { map } from 'rxjs/operators';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class StoresAdminService {

  private resourceUrl = '/api/source/git/v1';

  constructor(private http: HttpClient) {
  }

  findGitRepositories(): Observable<Array<GitRepository>> {
    return this.http.get(environment.backend + this.resourceUrl).pipe(map((res: GitRepository[]) => {
      const jsonResponse: GitRepository[] = res;
      jsonResponse.sort((a, b) => a.id < b.id ? -1 : 1);
      return jsonResponse;
    }));
  }

  saveRepository(repo: GitRepository): Observable<Object> {
    return this.http.post(environment.backend + this.resourceUrl, repo);
  }

  deleteRepository(repo: GitRepository): Observable<Object> {
    return this.http.delete(environment.backend + this.resourceUrl + '/' + repo.id);
  }
}
