import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { UserService } from '../../core/user/user.service';
import {
  PageReviewResponse,
  ReviewedReplacement,
  ReviewOptions,
  ReviewPage,
  SaveReviewRequest
} from './page-review.model';
import { ReplacementValidationResponse } from './validate-custom.model';

export const EMPTY_CONTENT = ' ';

@Injectable()
export class PageService {
  private readonly baseUrl = `${environment.apiUrl}/review`;

  constructor(private httpClient: HttpClient, private userService: UserService) {}

  findRandomPage(options: ReviewOptions): Observable<PageReviewResponse> {
    const params: HttpParams = this.mapReviewOptionsToHttpParams(options);
    return this.httpClient.get<PageReviewResponse>(`${this.baseUrl}/random`, { params });
  }

  private mapReviewOptionsToHttpParams(options: ReviewOptions): HttpParams {
    let params: HttpParams = new HttpParams();
    if (options.kind && options.subtype) {
      params = params.append('kind', options.kind).append('subtype', options.subtype);
      if (options.suggestion) {
        params = params.append('suggestion', options.suggestion).append('cs', String(options.cs));
      }
    }
    return params;
  }

  validateCustomReplacement(replacement: string, caseSensitive: boolean): Observable<ReplacementValidationResponse> {
    let params: HttpParams = new HttpParams();
    params = params.append('replacement', replacement).append('cs', String(caseSensitive));
    return this.httpClient.get<ReplacementValidationResponse>(`${environment.apiUrl}/replacement/type/validate`, {
      params
    });
  }

  findPageReviewById(pageId: number, options: ReviewOptions): Observable<PageReviewResponse> {
    const params: HttpParams = this.mapReviewOptionsToHttpParams(options);
    return this.httpClient.get<PageReviewResponse>(`${this.baseUrl}/${pageId}`, { params });
  }

  saveReview(page: ReviewPage, reviewedReplacements: ReviewedReplacement[]): Observable<void> {
    if (!this.userService.isValidUser()) {
      return throwError(() => new Error('El usuario no está autenticado. Recargue la página para retomar la sesión.'));
    }

    const saveReview = new SaveReviewRequest(page, reviewedReplacements, this.userService.accessToken);

    // Call backend and delay the observable response
    return this.httpClient.post<void>(`${this.baseUrl}/${page.id}`, saveReview);
  }
}
