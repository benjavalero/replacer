import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';

import { environment } from '../../environments/environment';
import { ArticleReview } from './article-review.model';
import { AuthenticationService } from '../authentication/authentication.service';

@Injectable({
  providedIn: 'root'
})
export class ArticleService {

  constructor(private httpClient: HttpClient, private authenticationService: AuthenticationService) { }

  findRandomArticle(type: string, subtype: string): Observable<number[]> {
    if (type && subtype) {
      return this.httpClient.get<number[]>(`${environment.apiUrl}/article/random/${type}/${subtype}`);
    } else {
      return this.httpClient.get<number[]>(`${environment.apiUrl}/article/random`);
    }
  }

  findArticleReviewById(articleId: number, type: string, subtype: string): Observable<ArticleReview> {
    if (type && subtype) {
      return this.httpClient.get<ArticleReview>(`${environment.apiUrl}/article/${articleId}/${type}/${subtype}`);
    } else {
      return this.httpClient.get<ArticleReview>(`${environment.apiUrl}/article/${articleId}`);
    }
  }

  saveArticle(articleId: number, content: string, currentTimestamp: string): Observable<any> {
    if (!this.authenticationService.isAuthenticated()) {
      return throwError('El usuario no está autenticado. Recargue la página para retomar la sesión.');
    }

    let params = new HttpParams();
    params = params.append('token', this.authenticationService.accessToken.token);
    params = params.append('tokenSecret', this.authenticationService.accessToken.tokenSecret);
    params = params.append('id', String(articleId));
    params = params.append('reviewer', this.authenticationService.user.name);
    params = params.append('currentTimestamp', currentTimestamp);

    return this.httpClient.put<any>(`${environment.apiUrl}/article`, content, { params });
  }

  findNumReplacements(): Observable<number> {
    return this.httpClient.get<number>(`${environment.apiUrl}/article/count/replacements`);
  }

  findNumNotReviewed(): Observable<number> {
    return this.httpClient.get<number>(`${environment.apiUrl}/article/count/replacements/to-review`);
  }

  findNumReviewed(): Observable<number> {
    return this.httpClient.get<number>(`${environment.apiUrl}/article/count/replacements/reviewed`);
  }

}
