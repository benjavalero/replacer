/* tslint:disable */
/* eslint-disable */
import { HttpClient, HttpContext } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { BaseService } from '../base-service';
import { ApiConfiguration } from '../api-configuration';
import { StrictHttpResponse } from '../strict-http-response';

import { countNotReviewedReplacementsGroupedByPage } from '../fn/replacement/count-not-reviewed-replacements-grouped-by-page';
import { CountNotReviewedReplacementsGroupedByPage$Params } from '../fn/replacement/count-not-reviewed-replacements-grouped-by-page';
import { countReplacements } from '../fn/replacement/count-replacements';
import { CountReplacements$Params } from '../fn/replacement/count-replacements';
import { countReviewedReplacementsGroupedByReviewer } from '../fn/replacement/count-reviewed-replacements-grouped-by-reviewer';
import { CountReviewedReplacementsGroupedByReviewer$Params } from '../fn/replacement/count-reviewed-replacements-grouped-by-reviewer';
import { PageCount } from '../models/page-count';
import { ReplacementCount } from '../models/replacement-count';
import { ReviewerCount } from '../models/reviewer-count';

@Injectable({ providedIn: 'root' })
export class ReplacementApiService extends BaseService {
  constructor(config: ApiConfiguration, http: HttpClient) {
    super(config, http);
  }

  /** Path part for operation `countReviewedReplacementsGroupedByReviewer()` */
  static readonly CountReviewedReplacementsGroupedByReviewerPath = '/api/replacement/user/count';

  /**
   * Count the number of reviewed replacements, including the custom ones, grouped by reviewer in descending order by count.
   *
   *
   *
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `countReviewedReplacementsGroupedByReviewer()` instead.
   *
   * This method doesn't expect any request body.
   */
  countReviewedReplacementsGroupedByReviewer$Response(params?: CountReviewedReplacementsGroupedByReviewer$Params, context?: HttpContext): Observable<StrictHttpResponse<Array<ReviewerCount>>> {
    return countReviewedReplacementsGroupedByReviewer(this.http, this.rootUrl, params, context);
  }

  /**
   * Count the number of reviewed replacements, including the custom ones, grouped by reviewer in descending order by count.
   *
   *
   *
   * This method provides access only to the response body.
   * To access the full response (for headers, for example), `countReviewedReplacementsGroupedByReviewer$Response()` instead.
   *
   * This method doesn't expect any request body.
   */
  countReviewedReplacementsGroupedByReviewer(params?: CountReviewedReplacementsGroupedByReviewer$Params, context?: HttpContext): Observable<Array<ReviewerCount>> {
    return this.countReviewedReplacementsGroupedByReviewer$Response(params, context).pipe(
      map((r: StrictHttpResponse<Array<ReviewerCount>>): Array<ReviewerCount> => r.body)
    );
  }

  /** Path part for operation `countNotReviewedReplacementsGroupedByPage()` */
  static readonly CountNotReviewedReplacementsGroupedByPagePath = '/api/replacement/page/count';

  /**
   * Count the number of replacements to review, including the custom ones, grouped by page in descending order by count.
   *
   *
   *
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `countNotReviewedReplacementsGroupedByPage()` instead.
   *
   * This method doesn't expect any request body.
   */
  countNotReviewedReplacementsGroupedByPage$Response(params?: CountNotReviewedReplacementsGroupedByPage$Params, context?: HttpContext): Observable<StrictHttpResponse<Array<PageCount>>> {
    return countNotReviewedReplacementsGroupedByPage(this.http, this.rootUrl, params, context);
  }

  /**
   * Count the number of replacements to review, including the custom ones, grouped by page in descending order by count.
   *
   *
   *
   * This method provides access only to the response body.
   * To access the full response (for headers, for example), `countNotReviewedReplacementsGroupedByPage$Response()` instead.
   *
   * This method doesn't expect any request body.
   */
  countNotReviewedReplacementsGroupedByPage(params?: CountNotReviewedReplacementsGroupedByPage$Params, context?: HttpContext): Observable<Array<PageCount>> {
    return this.countNotReviewedReplacementsGroupedByPage$Response(params, context).pipe(
      map((r: StrictHttpResponse<Array<PageCount>>): Array<PageCount> => r.body)
    );
  }

  /** Path part for operation `countReplacements()` */
  static readonly CountReplacementsPath = '/api/replacement/count';

  /**
   * Count the number of reviewed/unreviewed replacements including the custom ones.
   *
   *
   *
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `countReplacements()` instead.
   *
   * This method doesn't expect any request body.
   */
  countReplacements$Response(params: CountReplacements$Params, context?: HttpContext): Observable<StrictHttpResponse<ReplacementCount>> {
    return countReplacements(this.http, this.rootUrl, params, context);
  }

  /**
   * Count the number of reviewed/unreviewed replacements including the custom ones.
   *
   *
   *
   * This method provides access only to the response body.
   * To access the full response (for headers, for example), `countReplacements$Response()` instead.
   *
   * This method doesn't expect any request body.
   */
  countReplacements(params: CountReplacements$Params, context?: HttpContext): Observable<ReplacementCount> {
    return this.countReplacements$Response(params, context).pipe(
      map((r: StrictHttpResponse<ReplacementCount>): ReplacementCount => r.body)
    );
  }

}
