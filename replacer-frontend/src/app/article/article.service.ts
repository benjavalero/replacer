import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ArticleService {

  constructor(private httpClient: HttpClient) { }

  findRandomArticle(): Observable<number[]> {
    return this.httpClient.get<number[]>(`${environment.apiUrl}/article/random`);
  }

}
