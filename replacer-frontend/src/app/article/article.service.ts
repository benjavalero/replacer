import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { ArticleReview } from './article-review.model';
import { AuthenticationService } from '../authentication/authentication.service';

@Injectable({
  providedIn: 'root'
})
export class ArticleService {

  constructor(private httpClient: HttpClient, private authenticationService: AuthenticationService) { }

  findRandomArticle(): Observable<number[]> {
    return this.httpClient.get<number[]>(`${environment.apiUrl}/article/random`);
  }

  findArticleReviewById(articleId: number): Observable<ArticleReview> {
    return this.httpClient.get<ArticleReview>(`${environment.apiUrl}/article/review/${articleId}`);
  }

  saveArticle(articleId: number, content: string): Observable<boolean> {
    let params = new HttpParams();
    params = params.append('token', this.authenticationService.accessToken.token);
    params = params.append('tokenSecret', this.authenticationService.accessToken.tokenSecret);
    params = params.append('id', String(articleId));

    return this.httpClient.put<boolean>(`${environment.apiUrl}/article`, content, { params });
  }

}
