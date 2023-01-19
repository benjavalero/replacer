/* tslint:disable */
/* eslint-disable */
import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse, HttpContext } from '@angular/common/http';
import { BaseService } from '../base-service';
import { ApiConfiguration } from '../api-configuration';
import { StrictHttpResponse } from '../strict-http-response';
import { RequestBuilder } from '../request-builder';
import { Observable } from 'rxjs';
import { map, filter } from 'rxjs/operators';

import { PageCount } from '../models/page-count';
import { ReplacementCount } from '../models/replacement-count';
import { ReplacementType } from '../models/replacement-type';
import { ReviewerCount } from '../models/reviewer-count';

@Injectable({
  providedIn: 'root',
})
export class ReplacementsService extends BaseService {
  constructor(
    config: ApiConfiguration,
    http: HttpClient
  ) {
    super(config, http);
  }

  /**
   * Path part for operation countReplacementsGroupedByReviewer
   */
  static readonly CountReplacementsGroupedByReviewerPath = '/api/replacement/user/count';

  /**
   * Count the number of reviewed replacements grouped by reviewer in descending order by count.
   *
   *
   *
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `countReplacementsGroupedByReviewer()` instead.
   *
   * This method doesn't expect any request body.
   */
  countReplacementsGroupedByReviewer$Response(params?: {
    context?: HttpContext
  }
): Observable<StrictHttpResponse<Array<ReviewerCount>>> {

    const rb = new RequestBuilder(this.rootUrl, ReplacementsService.CountReplacementsGroupedByReviewerPath, 'get');
    if (params) {
    }

    return this.http.request(rb.build({
      responseType: 'json',
      accept: 'application/json',
      context: params?.context
    })).pipe(
      filter((r: any) => r instanceof HttpResponse),
      map((r: HttpResponse<any>) => {
        return r as StrictHttpResponse<Array<ReviewerCount>>;
      })
    );
  }

  /**
   * Count the number of reviewed replacements grouped by reviewer in descending order by count.
   *
   *
   *
   * This method provides access to only to the response body.
   * To access the full response (for headers, for example), `countReplacementsGroupedByReviewer$Response()` instead.
   *
   * This method doesn't expect any request body.
   */
  countReplacementsGroupedByReviewer(params?: {
    context?: HttpContext
  }
): Observable<Array<ReviewerCount>> {

    return this.countReplacementsGroupedByReviewer$Response(params).pipe(
      map((r: StrictHttpResponse<Array<ReviewerCount>>) => r.body as Array<ReviewerCount>)
    );
  }

  /**
   * Path part for operation validateCustomReplacement
   */
  static readonly ValidateCustomReplacementPath = '/api/replacement/type/validate';

  /**
   * Validate if the custom replacement matches with a known replacement type.
   *
   *
   *
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `validateCustomReplacement()` instead.
   *
   * This method doesn't expect any request body.
   */
  validateCustomReplacement$Response(params: {

    /**
     * Replacement to validate
     */
    replacement: string;

    /**
     * If the custom replacement is case-sensitive
     */
    cs: boolean;
    context?: HttpContext
  }
): Observable<StrictHttpResponse<ReplacementType>> {

    const rb = new RequestBuilder(this.rootUrl, ReplacementsService.ValidateCustomReplacementPath, 'get');
    if (params) {
      rb.query('replacement', params.replacement, {});
      rb.query('cs', params.cs, {});
    }

    return this.http.request(rb.build({
      responseType: 'json',
      accept: 'application/json',
      context: params?.context
    })).pipe(
      filter((r: any) => r instanceof HttpResponse),
      map((r: HttpResponse<any>) => {
        return r as StrictHttpResponse<ReplacementType>;
      })
    );
  }

  /**
   * Validate if the custom replacement matches with a known replacement type.
   *
   *
   *
   * This method provides access to only to the response body.
   * To access the full response (for headers, for example), `validateCustomReplacement$Response()` instead.
   *
   * This method doesn't expect any request body.
   */
  validateCustomReplacement(params: {

    /**
     * Replacement to validate
     */
    replacement: string;

    /**
     * If the custom replacement is case-sensitive
     */
    cs: boolean;
    context?: HttpContext
  }
): Observable<ReplacementType> {

    return this.validateCustomReplacement$Response(params).pipe(
      map((r: StrictHttpResponse<ReplacementType>) => r.body as ReplacementType)
    );
  }

  /**
   * Path part for operation countNotReviewedGroupedByPage
   */
  static readonly CountNotReviewedGroupedByPagePath = '/api/replacement/page/count';

  /**
   * Count the number of replacements to review, including the custom ones, grouped by page in descending order by count.
   *
   *
   *
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `countNotReviewedGroupedByPage()` instead.
   *
   * This method doesn't expect any request body.
   */
  countNotReviewedGroupedByPage$Response(params?: {
    context?: HttpContext
  }
): Observable<StrictHttpResponse<Array<PageCount>>> {

    const rb = new RequestBuilder(this.rootUrl, ReplacementsService.CountNotReviewedGroupedByPagePath, 'get');
    if (params) {
    }

    return this.http.request(rb.build({
      responseType: 'json',
      accept: 'application/json',
      context: params?.context
    })).pipe(
      filter((r: any) => r instanceof HttpResponse),
      map((r: HttpResponse<any>) => {
        return r as StrictHttpResponse<Array<PageCount>>;
      })
    );
  }

  /**
   * Count the number of replacements to review, including the custom ones, grouped by page in descending order by count.
   *
   *
   *
   * This method provides access to only to the response body.
   * To access the full response (for headers, for example), `countNotReviewedGroupedByPage$Response()` instead.
   *
   * This method doesn't expect any request body.
   */
  countNotReviewedGroupedByPage(params?: {
    context?: HttpContext
  }
): Observable<Array<PageCount>> {

    return this.countNotReviewedGroupedByPage$Response(params).pipe(
      map((r: StrictHttpResponse<Array<PageCount>>) => r.body as Array<PageCount>)
    );
  }

  /**
   * Path part for operation countReplacements
   */
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
  countReplacements$Response(params: {

    /**
     * Filter by reviewed/unreviewed replacements
     */
    reviewed: boolean;
    context?: HttpContext
  }
): Observable<StrictHttpResponse<ReplacementCount>> {

    const rb = new RequestBuilder(this.rootUrl, ReplacementsService.CountReplacementsPath, 'get');
    if (params) {
      rb.query('reviewed', params.reviewed, {});
    }

    return this.http.request(rb.build({
      responseType: 'json',
      accept: 'application/json',
      context: params?.context
    })).pipe(
      filter((r: any) => r instanceof HttpResponse),
      map((r: HttpResponse<any>) => {
        return r as StrictHttpResponse<ReplacementCount>;
      })
    );
  }

  /**
   * Count the number of reviewed/unreviewed replacements including the custom ones.
   *
   *
   *
   * This method provides access to only to the response body.
   * To access the full response (for headers, for example), `countReplacements$Response()` instead.
   *
   * This method doesn't expect any request body.
   */
  countReplacements(params: {

    /**
     * Filter by reviewed/unreviewed replacements
     */
    reviewed: boolean;
    context?: HttpContext
  }
): Observable<ReplacementCount> {

    return this.countReplacements$Response(params).pipe(
      map((r: StrictHttpResponse<ReplacementCount>) => r.body as ReplacementCount)
    );
  }

}
