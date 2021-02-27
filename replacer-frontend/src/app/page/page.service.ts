import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import { UserService } from '../user/user.service';
import { PageReview } from './page-review.model';
import { SavePage } from './save-page.model';

@Injectable({
  providedIn: 'root'
})
export class PageService {
  baseUrl = `${environment.apiUrl}/pages`;

  private cachedPageReviews = {};

  constructor(private httpClient: HttpClient, private userService: UserService) {}

  findRandomPage(type: string, subtype: string, suggestion: string, caseSensitive: boolean): Observable<PageReview> {
    console.log('Find random service');
    console.log(suggestion, caseSensitive);

    let params: HttpParams = new HttpParams();
    if (type && subtype) {
      if (suggestion) {
        params = params
          .append('replacement', subtype)
          .append('suggestion', suggestion)
          .append('cs', String(caseSensitive));
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

  validateCustomReplacement(replacement: string): Observable<string> {
    let params: HttpParams = new HttpParams();
    params = params.append('replacement', replacement);
    return this.httpClient.get<string>(`${this.baseUrl}/validate`, { params });
  }

  findPageReviewById(
    pageId: number,
    type: string,
    subtype: string,
    suggestion: string,
    caseSensitive: boolean
  ): Observable<PageReview> {
    let params: HttpParams = new HttpParams();
    if (type && subtype) {
      if (suggestion) {
        params = params
          .append('replacement', subtype)
          .append('suggestion', suggestion)
          .append('cs', String(caseSensitive));
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
    title: string,
    content: string,
    section: number,
    currentTimestamp: string
  ): Observable<any> {
    if (!this.userService.isValidUser()) {
      return throwError('El usuario no está autenticado. Recargue la página para retomar la sesión.');
    }

    const savePage = new SavePage();
    if (section) {
      savePage.section = section;
    }
    savePage.title = title;
    savePage.content = content;
    savePage.timestamp = currentTimestamp;
    savePage.token = this.userService.accessToken.token;
    savePage.tokenSecret = this.userService.accessToken.tokenSecret;
    if (type && subtype) {
      savePage.type = type;
      savePage.subtype = subtype;
    }

    return this.httpClient.post<string>(`${this.baseUrl}/${pageId}`, savePage);
  }
}
