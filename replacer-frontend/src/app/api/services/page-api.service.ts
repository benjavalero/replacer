/* tslint:disable */
/* eslint-disable */
import { HttpClient, HttpContext } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { BaseService } from '../base-service';
import { ApiConfiguration } from '../api-configuration';
import { StrictHttpResponse } from '../strict-http-response';

import { countNotReviewedGroupedByType } from '../fn/page/count-not-reviewed-grouped-by-type';
import { CountNotReviewedGroupedByType$Params } from '../fn/page/count-not-reviewed-grouped-by-type';
import { findPageReviewById } from '../fn/page/find-page-review-by-id';
import { FindPageReviewById$Params } from '../fn/page/find-page-review-by-id';
import { findRandomPageWithReplacements } from '../fn/page/find-random-page-with-replacements';
import { FindRandomPageWithReplacements$Params } from '../fn/page/find-random-page-with-replacements';
import { KindCount } from '../models/kind-count';
import { listPageTitlesNotReviewedByType } from '../fn/page/list-page-titles-not-reviewed-by-type';
import { ListPageTitlesNotReviewedByType$Params } from '../fn/page/list-page-titles-not-reviewed-by-type';
import { Page } from '../models/page';
import { reviewPagesByType } from '../fn/page/review-pages-by-type';
import { ReviewPagesByType$Params } from '../fn/page/review-pages-by-type';
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

  /** Path part for operation `listPageTitlesNotReviewedByType()` */
  static readonly ListPageTitlesNotReviewedByTypePath = '/api/page/type';

  /**
   * List the titles of pages to review containing the given replacement type.
   *
   *
   *
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `listPageTitlesNotReviewedByType()` instead.
   *
   * This method doesn't expect any request body.
   */
  listPageTitlesNotReviewedByType$Response(params: ListPageTitlesNotReviewedByType$Params, context?: HttpContext): Observable<StrictHttpResponse<string>> {
    return listPageTitlesNotReviewedByType(this.http, this.rootUrl, params, context);
  }

  /**
   * List the titles of pages to review containing the given replacement type.
   *
   *
   *
   * This method provides access only to the response body.
   * To access the full response (for headers, for example), `listPageTitlesNotReviewedByType$Response()` instead.
   *
   * This method doesn't expect any request body.
   */
  listPageTitlesNotReviewedByType(params: ListPageTitlesNotReviewedByType$Params, context?: HttpContext): Observable<string> {
    return this.listPageTitlesNotReviewedByType$Response(params, context).pipe(
      map((r: StrictHttpResponse<string>): string => r.body)
    );
  }

  /** Path part for operation `reviewPagesByType()` */
  static readonly ReviewPagesByTypePath = '/api/page/type';

  /**
   * Mark as reviewed the pages containing the given replacement type.
   *
   *
   *
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `reviewPagesByType()` instead.
   *
   * This method doesn't expect any request body.
   */
  reviewPagesByType$Response(params: ReviewPagesByType$Params, context?: HttpContext): Observable<StrictHttpResponse<void>> {
    return reviewPagesByType(this.http, this.rootUrl, params, context);
  }

  /**
   * Mark as reviewed the pages containing the given replacement type.
   *
   *
   *
   * This method provides access only to the response body.
   * To access the full response (for headers, for example), `reviewPagesByType$Response()` instead.
   *
   * This method doesn't expect any request body.
   */
  reviewPagesByType(params: ReviewPagesByType$Params, context?: HttpContext): Observable<void> {
    return this.reviewPagesByType$Response(params, context).pipe(
      map((r: StrictHttpResponse<void>): void => r.body)
    );
  }

  /** Path part for operation `countNotReviewedGroupedByType()` */
  static readonly CountNotReviewedGroupedByTypePath = '/api/page/type/count';

  /**
   * Count the number of pages to review grouped by replacement type.
   *
   *
   *
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `countNotReviewedGroupedByType()` instead.
   *
   * This method doesn't expect any request body.
   */
  countNotReviewedGroupedByType$Response(params?: CountNotReviewedGroupedByType$Params, context?: HttpContext): Observable<StrictHttpResponse<Array<KindCount>>> {
    return countNotReviewedGroupedByType(this.http, this.rootUrl, params, context);
  }

  /**
   * Count the number of pages to review grouped by replacement type.
   *
   *
   *
   * This method provides access only to the response body.
   * To access the full response (for headers, for example), `countNotReviewedGroupedByType$Response()` instead.
   *
   * This method doesn't expect any request body.
   */
  countNotReviewedGroupedByType(params?: CountNotReviewedGroupedByType$Params, context?: HttpContext): Observable<Array<KindCount>> {
    return this.countNotReviewedGroupedByType$Response(params, context).pipe(
      map((r: StrictHttpResponse<Array<KindCount>>): Array<KindCount> => r.body)
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
