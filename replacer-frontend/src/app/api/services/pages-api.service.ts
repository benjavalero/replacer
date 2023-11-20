/* tslint:disable */
/* eslint-disable */
import { HttpClient, HttpContext } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { BaseService } from '../base-service';
import { ApiConfiguration } from '../api-configuration';
import { StrictHttpResponse } from '../strict-http-response';

import { countNotReviewedGroupedByType } from '../fn/pages/count-not-reviewed-grouped-by-type';
import { CountNotReviewedGroupedByType$Params } from '../fn/pages/count-not-reviewed-grouped-by-type';
import { findPageTitlesNotReviewedByType } from '../fn/pages/find-page-titles-not-reviewed-by-type';
import { FindPageTitlesNotReviewedByType$Params } from '../fn/pages/find-page-titles-not-reviewed-by-type';
import { KindCount } from '../models/kind-count';
import { reviewPagesByType } from '../fn/pages/review-pages-by-type';
import { ReviewPagesByType$Params } from '../fn/pages/review-pages-by-type';

@Injectable({ providedIn: 'root' })
export class PagesApiService extends BaseService {
  constructor(config: ApiConfiguration, http: HttpClient) {
    super(config, http);
  }

  /** Path part for operation `findPageTitlesNotReviewedByType()` */
  static readonly FindPageTitlesNotReviewedByTypePath = '/api/page/type';

  /**
   * List the pages to review containing the given replacement type.
   *
   *
   *
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `findPageTitlesNotReviewedByType()` instead.
   *
   * This method doesn't expect any request body.
   */
  findPageTitlesNotReviewedByType$Response(params: FindPageTitlesNotReviewedByType$Params, context?: HttpContext): Observable<StrictHttpResponse<string>> {
    return findPageTitlesNotReviewedByType(this.http, this.rootUrl, params, context);
  }

  /**
   * List the pages to review containing the given replacement type.
   *
   *
   *
   * This method provides access only to the response body.
   * To access the full response (for headers, for example), `findPageTitlesNotReviewedByType$Response()` instead.
   *
   * This method doesn't expect any request body.
   */
  findPageTitlesNotReviewedByType(params: FindPageTitlesNotReviewedByType$Params, context?: HttpContext): Observable<string> {
    return this.findPageTitlesNotReviewedByType$Response(params, context).pipe(
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

}
