import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';

import { environment } from '../../environments/environment';
import { ArticleReview } from './article-review.model';
import { AuthenticationService } from '../authentication/authentication.service';
import { SaveArticle } from './save-article.model';

@Injectable({
  providedIn: 'root'
})
export class ArticleService {
  baseUrl = `${environment.apiUrl}/pages`;

  private cachedArticleReviews = {};

  constructor(private httpClient: HttpClient, private authenticationService: AuthenticationService) {}

  findRandomArticle(type: string, subtype: string, suggestion: string): Observable<ArticleReview> {
    if (type && subtype) {
      if (suggestion) {
        return this.httpClient.get<ArticleReview>(`${this.baseUrl}/random/${type}/${subtype}/${suggestion}`);
      } else {
        return this.httpClient.get<ArticleReview>(`${this.baseUrl}/random/${type}/${subtype}`);
      }
    } else {
      return this.httpClient.get<ArticleReview>(`${this.baseUrl}/random`);
    }
  }

  getArticleReviewFromCache(articleId: number, type: string, subtype: string): ArticleReview {
    const key = this.buildReviewCacheKey(articleId, type, subtype);
    const review = this.cachedArticleReviews[key];
    delete this.cachedArticleReviews[key];
    return review;
  }

  putArticleReviewInCache(type: string, subtype: string, review: ArticleReview): void {
    const key = this.buildReviewCacheKey(review.id, type, subtype);
    this.cachedArticleReviews[key] = review;
  }

  private buildReviewCacheKey(articleId: number, type: string, subtype: string): string {
    if (type && subtype) {
      return `${articleId}-${type}-${subtype}`;
    } else {
      return String(articleId);
    }
  }

  findArticleReviewById(
    articleId: number,
    type: string,
    subtype: string,
    suggestion: string
  ): Observable<ArticleReview> {
    if (type && subtype) {
      if (suggestion) {
        return this.httpClient.get<ArticleReview>(`${this.baseUrl}/${articleId}/${type}/${subtype}/${suggestion}`);
      } else {
        return this.httpClient.get<ArticleReview>(`${this.baseUrl}/${articleId}/${type}/${subtype}`);
      }
    } else {
      return this.httpClient.get<ArticleReview>(`${this.baseUrl}/${articleId}`);
    }
  }

  saveArticle(
    articleId: number,
    type: string,
    subtype: string,
    content: string,
    section: number,
    currentTimestamp: string
  ): Observable<any> {
    if (!this.authenticationService.isAuthenticated()) {
      return throwError('El usuario no está autenticado. Recargue la página para retomar la sesión.');
    }

    const saveArticle = new SaveArticle();
    saveArticle.articleId = articleId;
    if (section) {
      saveArticle.section = section;
    }
    saveArticle.content = content;
    saveArticle.timestamp = currentTimestamp;
    saveArticle.reviewer = this.authenticationService.user.name;
    saveArticle.token = this.authenticationService.accessToken;
    if (type && subtype) {
      saveArticle.type = type;
      saveArticle.subtype = subtype;
    }

    return this.httpClient.post<any>(`${this.baseUrl}`, saveArticle);
  }
}
