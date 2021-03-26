import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import { UserService } from '../user/user.service';
import { PageDto, PageReview, PageSearch, SavePage } from './page-review.model';
import { ValidateType } from './validate-custom.model';

@Injectable({
  providedIn: 'root'
})
export class PageService {
  baseUrl = `${environment.apiUrl}/pages`;

  private cachedPageReviews = {};

  constructor(private httpClient: HttpClient, private userService: UserService) {}

  findRandomPage(type: string, subtype: string, suggestion: string, caseSensitive: boolean): Observable<PageReview> {
    let params: HttpParams = new HttpParams();
    if (type && subtype) {
      params = params.append('type', type).append('subtype', subtype);
      if (suggestion) {
        params = params.append('suggestion', suggestion).append('cs', String(caseSensitive));
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
    const key = this.buildReviewCacheKey(review.page.id, type, subtype);
    this.cachedPageReviews[key] = review;
  }

  private buildReviewCacheKey(pageId: number, type: string, subtype: string): string {
    if (type && subtype) {
      return `${pageId}-${type}-${subtype}`;
    } else {
      return String(pageId);
    }
  }

  validateCustomReplacement(replacement: string, caseSensitive: boolean): Observable<ValidateType> {
    let params: HttpParams = new HttpParams();
    params = params.append('replacement', replacement).append('cs', String(caseSensitive));
    return this.httpClient.get<ValidateType>(`${this.baseUrl}/validate`, { params });
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
      params = params.append('type', type).append('subtype', subtype);
      if (suggestion) {
        params = params.append('suggestion', suggestion).append('cs', String(caseSensitive));
      }
    }
    return this.httpClient.get<PageReview>(`${this.baseUrl}/${pageId}`, { params });
  }

  savePage(page: PageDto, search: PageSearch): Observable<any> {
    if (!this.userService.isValidUser()) {
      return throwError('El usuario no está autenticado. Recargue la página para retomar la sesión.');
    }

    const savePage = new SavePage();
    savePage.page = page;
    savePage.search = search;
    savePage.accessToken = this.userService.accessToken;

    return this.httpClient.post<string>(`${this.baseUrl}/${page.id}`, savePage);
  }
}
