import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { delay } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { UserService } from '../user/user.service';
import { PageDto, PageReview, PageReviewSearch, ReviewOptions, SavePage } from './page-review.model';
import { ValidateType } from './validate-custom.model';

@Injectable({
  providedIn: 'root'
})
export class PageService {
  private readonly lastSaveKey = 'lastSave';
  private readonly editionsPerMinute = 5;
  private readonly baseUrl = `${environment.apiUrl}/pages`;

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

  savePage(page: PageDto, search: PageReviewSearch): Observable<any> {
    if (!this.userService.isValidUser()) {
      return throwError('El usuario no está autenticado. Recargue la página para retomar la sesión.');
    }

    // Store the date of the last save to check there are at least 12 s between savings (5 editions/min)
    let sleepTime = 0;
    const isBotUser = this.userService.isBotUser();
    const lastSave = localStorage.getItem(this.lastSaveKey);
    if (!isBotUser && lastSave && page.content !== ' ') {
      const minGap: number = (1000 * 60) / this.editionsPerMinute;
      const lastSaveDate: number = +lastSave;
      const gap: number = Date.now() - lastSaveDate;
      if (gap < minGap) {
        sleepTime = minGap - gap;
      }
    }

    const savePage = new SavePage(
      page,
      search,
      this.userService.accessToken.token,
      this.userService.accessToken.tokenSecret
    );

    // Store the new last save date
    if (page.content !== ' ') {
      localStorage.setItem(this.lastSaveKey, String(Date.now()));
    }

    // Call backend and delay the observable response
    return this.httpClient.post<string>(`${this.baseUrl}/${page.id}`, savePage).pipe(delay(sleepTime));
  }
}
