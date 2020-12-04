import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthenticationService } from '../authentication/authentication.service';
import { PageReview } from './page-review.model';
import { SavePage } from './save-page.model';

@Injectable({
  providedIn: 'root'
})
export class ArticleService {
  baseUrl = `${environment.apiUrl}/pages`;

  private cachedPageReviews = {};

  constructor(private httpClient: HttpClient, private authenticationService: AuthenticationService) {}

  findRandomArticle(type: string, subtype: string, suggestion: string): Observable<PageReview> {
    let params: HttpParams = new HttpParams();
    if (type && subtype) {
      if (suggestion) {
        params = params.append('replacement', subtype).append('suggestion', suggestion);
      } else {
        params = params.append('type', type).append('subtype', subtype);
      }
    }
    return this.httpClient.get<PageReview>(`${this.baseUrl}/random`, { params });
  }

  getPageReviewFromCache(pageId: number, type: string, subtype: string): PageReview {
    const key = this.buildReviewCacheKey(pageId, type, subtype);
    const review = this.cachedPageReviews[key];
    delete this.cachedPageReviews[key];
    return review;
  }

  putPageReviewInCache(type: string, subtype: string, review: PageReview): void {
    const key = this.buildReviewCacheKey(review.id, type, subtype);
    this.cachedPageReviews[key] = review;
  }

  private buildReviewCacheKey(pageId: number, type: string, subtype: string): string {
    if (type && subtype) {
      return `${pageId}-${type}-${subtype}`;
    } else {
      return String(pageId);
    }
  }

  findPageReviewById(pageId: number, type: string, subtype: string, suggestion: string): Observable<PageReview> {
    let params: HttpParams = new HttpParams();
    if (type && subtype) {
      if (suggestion) {
        params = params.append('replacement', subtype).append('suggestion', suggestion);
      } else {
        params = params.append('type', type).append('subtype', subtype);
      }
    }
    return this.httpClient.get<PageReview>(`${this.baseUrl}/${pageId}`, { params });
  }

  savePage(
    pageId: number,
    type: string,
    subtype: string,
    content: string,
    section: number,
    currentTimestamp: string
  ): Observable<any> {
    if (!this.authenticationService.isAuthenticated()) {
      return throwError('El usuario no está autenticado. Recargue la página para retomar la sesión.');
    }

    const savePage = new SavePage();
    if (section) {
      savePage.section = section;
    }
    savePage.content = content;
    savePage.timestamp = currentTimestamp;
    savePage.reviewer = this.authenticationService.user.name;
    savePage.token = this.authenticationService.accessToken;
    if (type && subtype) {
      savePage.type = type;
      savePage.subtype = subtype;
    }

    return this.httpClient.post<string>(`${this.baseUrl}/${pageId}`, savePage);
  }

  reviewPages(type: string, subtype: string): Observable<any> {
    let params: HttpParams = new HttpParams();
    params = params.append('type', type).append('subtype', subtype);
    return this.httpClient.post<any>(`${this.baseUrl}/review`, null, { params });
  }
}
