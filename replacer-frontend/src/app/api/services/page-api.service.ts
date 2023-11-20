/* tslint:disable */
/* eslint-disable */
import { HttpClient, HttpContext } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { BaseService } from '../base-service';
import { ApiConfiguration } from '../api-configuration';
import { StrictHttpResponse } from '../strict-http-response';

import { findPageReviewById } from '../fn/page/find-page-review-by-id';
import { FindPageReviewById$Params } from '../fn/page/find-page-review-by-id';
import { findRandomPageWithReplacements } from '../fn/page/find-random-page-with-replacements';
import { FindRandomPageWithReplacements$Params } from '../fn/page/find-random-page-with-replacements';
import { Page } from '../models/page';
import { saveReview } from '../fn/page/save-review';
import { SaveReview$Params } from '../fn/page/save-review';

@Injectable({ providedIn: 'root' })
export class PageApiService extends BaseService {
  constructor(config: ApiConfiguration, http: HttpClient) {
    super(config, http);
  }

  /** Path part for operation `findPageReviewById()` */
  static readonly FindPageReviewByIdPath = '/api/page/{id}';

  /**
   * Find a page and the replacements to review.
   *
   *
   *
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `findPageReviewById()` instead.
   *
   * This method doesn't expect any request body.
   */
  findPageReviewById$Response(params: FindPageReviewById$Params, context?: HttpContext): Observable<StrictHttpResponse<Page>> {
    return findPageReviewById(this.http, this.rootUrl, params, context);
  }

  /**
   * Find a page and the replacements to review.
   *
   *
   *
   * This method provides access only to the response body.
   * To access the full response (for headers, for example), `findPageReviewById$Response()` instead.
   *
   * This method doesn't expect any request body.
   */
  findPageReviewById(params: FindPageReviewById$Params, context?: HttpContext): Observable<Page> {
    return this.findPageReviewById$Response(params, context).pipe(
      map((r: StrictHttpResponse<Page>): Page => r.body)
    );
  }

  /** Path part for operation `saveReview()` */
  static readonly SaveReviewPath = '/api/page/{id}';

  /**
   * Save a review: update page contents and mark as reviewed.
   *
   *
   *
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `saveReview()` instead.
   *
   * This method sends `application/json` and handles request body of type `application/json`.
   */
  saveReview$Response(params: SaveReview$Params, context?: HttpContext): Observable<StrictHttpResponse<void>> {
    return saveReview(this.http, this.rootUrl, params, context);
  }

  /**
   * Save a review: update page contents and mark as reviewed.
   *
   *
   *
   * This method provides access only to the response body.
   * To access the full response (for headers, for example), `saveReview$Response()` instead.
   *
   * This method sends `application/json` and handles request body of type `application/json`.
   */
  saveReview(params: SaveReview$Params, context?: HttpContext): Observable<void> {
    return this.saveReview$Response(params, context).pipe(
      map((r: StrictHttpResponse<void>): void => r.body)
    );
  }

  /** Path part for operation `findRandomPageWithReplacements()` */
  static readonly FindRandomPageWithReplacementsPath = '/api/page/random';

  /**
   * Find a random page and the replacements to review.
   *
   *
   *
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `findRandomPageWithReplacements()` instead.
   *
   * This method doesn't expect any request body.
   */
  findRandomPageWithReplacements$Response(params?: FindRandomPageWithReplacements$Params, context?: HttpContext): Observable<StrictHttpResponse<Page>> {
    return findRandomPageWithReplacements(this.http, this.rootUrl, params, context);
  }

  /**
   * Find a random page and the replacements to review.
   *
   *
   *
   * This method provides access only to the response body.
   * To access the full response (for headers, for example), `findRandomPageWithReplacements$Response()` instead.
   *
   * This method doesn't expect any request body.
   */
  findRandomPageWithReplacements(params?: FindRandomPageWithReplacements$Params, context?: HttpContext): Observable<Page> {
    return this.findRandomPageWithReplacements$Response(params, context).pipe(
      map((r: StrictHttpResponse<Page>): Page => r.body)
    );
  }

}
