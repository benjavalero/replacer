import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import { UserService } from '../user/user.service';
import { PageDto, PageReview, PageSearch, ReviewOptions, SavePage } from './page-review.model';
import { ValidateType } from './validate-custom.model';

@Injectable({
  providedIn: 'root'
})
export class PageService {
  baseUrl = `${environment.apiUrl}/pages`;

  constructor(private httpClient: HttpClient, private userService: UserService) {}

  findRandomPage(options: ReviewOptions): Observable<PageReview> {
    let params: HttpParams = new HttpParams();
    if (options.type && options.subtype) {
      params = params.append('type', options.type).append('subtype', options.subtype);
      if (options.suggestion) {
        params = params.append('suggestion', options.suggestion).append('cs', String(options.cs));
      }
    }
    return this.httpClient.get<PageReview>(`${this.baseUrl}/random`, { params });
  }

  validateCustomReplacement(replacement: string, caseSensitive: boolean): Observable<ValidateType> {
    let params: HttpParams = new HttpParams();
    params = params.append('replacement', replacement).append('cs', String(caseSensitive));
    return this.httpClient.get<ValidateType>(`${this.baseUrl}/validate`, { params });
  }

  findPageReviewById(pageId: number, options: ReviewOptions): Observable<PageReview> {
    let params: HttpParams = new HttpParams();
    if (options.type && options.subtype) {
      params = params.append('type', options.type).append('subtype', options.subtype);
      if (options.suggestion) {
        params = params.append('suggestion', options.suggestion).append('cs', String(options.cs));
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
