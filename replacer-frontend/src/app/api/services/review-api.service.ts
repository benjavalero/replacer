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

import { FindReviewResponse } from '../models/find-review-response';
import { SaveReviewRequest } from '../models/save-review-request';

@Injectable({
  providedIn: 'root',
})
export class ReviewApiService extends BaseService {
  constructor(
    config: ApiConfiguration,
    http: HttpClient
  ) {
    super(config, http);
  }

  /**
   * Path part for operation findPageReviewById
   */
  static readonly FindPageReviewByIdPath = '/api/review/{id}';

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
  findPageReviewById$Response(params: {

    /**
     * Page ID
     */
    id: number;

    /**
     * Replacement kind code
     */
    kind?: number;

    /**
     * Replacement subtype
     */
    subtype?: string;

    /**
     * If the custom replacement is case-sensitive
     */
    cs?: boolean;

    /**
     * Custom replacement suggestion
     */
    suggestion?: string;
    context?: HttpContext
  }
): Observable<StrictHttpResponse<FindReviewResponse>> {

    const rb = new RequestBuilder(this.rootUrl, ReviewApiService.FindPageReviewByIdPath, 'get');
    if (params) {
      rb.path('id', params.id, {});
      rb.query('kind', params.kind, {});
      rb.query('subtype', params.subtype, {});
      rb.query('cs', params.cs, {});
      rb.query('suggestion', params.suggestion, {});
    }

    return this.http.request(rb.build({
      responseType: 'json',
      accept: 'application/json',
      context: params?.context
    })).pipe(
      filter((r: any) => r instanceof HttpResponse),
      map((r: HttpResponse<any>) => {
        return r as StrictHttpResponse<FindReviewResponse>;
      })
    );
  }

  /**
   * Find a page and the replacements to review.
   *
   *
   *
   * This method provides access to only to the response body.
   * To access the full response (for headers, for example), `findPageReviewById$Response()` instead.
   *
   * This method doesn't expect any request body.
   */
  findPageReviewById(params: {

    /**
     * Page ID
     */
    id: number;

    /**
     * Replacement kind code
     */
    kind?: number;

    /**
     * Replacement subtype
     */
    subtype?: string;

    /**
     * If the custom replacement is case-sensitive
     */
    cs?: boolean;

    /**
     * Custom replacement suggestion
     */
    suggestion?: string;
    context?: HttpContext
  }
): Observable<FindReviewResponse> {

    return this.findPageReviewById$Response(params).pipe(
      map((r: StrictHttpResponse<FindReviewResponse>) => r.body as FindReviewResponse)
    );
  }

  /**
   * Path part for operation saveReview
   */
  static readonly SaveReviewPath = '/api/review/{id}';

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
  saveReview$Response(params: {

    /**
     * Page ID
     */
    id: number;
    context?: HttpContext
    body: SaveReviewRequest
  }
): Observable<StrictHttpResponse<void>> {

    const rb = new RequestBuilder(this.rootUrl, ReviewApiService.SaveReviewPath, 'post');
    if (params) {
      rb.path('id', params.id, {});
      rb.body(params.body, 'application/json');
    }

    return this.http.request(rb.build({
      responseType: 'text',
      accept: '*/*',
      context: params?.context
    })).pipe(
      filter((r: any) => r instanceof HttpResponse),
      map((r: HttpResponse<any>) => {
        return (r as HttpResponse<any>).clone({ body: undefined }) as StrictHttpResponse<void>;
      })
    );
  }

  /**
   * Save a review: update page contents and mark as reviewed.
   *
   *
   *
   * This method provides access to only to the response body.
   * To access the full response (for headers, for example), `saveReview$Response()` instead.
   *
   * This method sends `application/json` and handles request body of type `application/json`.
   */
  saveReview(params: {

    /**
     * Page ID
     */
    id: number;
    context?: HttpContext
    body: SaveReviewRequest
  }
): Observable<void> {

    return this.saveReview$Response(params).pipe(
      map((r: StrictHttpResponse<void>) => r.body as void)
    );
  }

  /**
   * Path part for operation findRandomPageWithReplacements
   */
  static readonly FindRandomPageWithReplacementsPath = '/api/review/random';

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
  findRandomPageWithReplacements$Response(params?: {

    /**
     * Replacement kind code
     */
    kind?: number;

    /**
     * Replacement subtype
     */
    subtype?: string;

    /**
     * If the custom replacement is case-sensitive
     */
    cs?: boolean;

    /**
     * Custom replacement suggestion
     */
    suggestion?: string;
    context?: HttpContext
  }
): Observable<StrictHttpResponse<FindReviewResponse>> {

    const rb = new RequestBuilder(this.rootUrl, ReviewApiService.FindRandomPageWithReplacementsPath, 'get');
    if (params) {
      rb.query('kind', params.kind, {});
      rb.query('subtype', params.subtype, {});
      rb.query('cs', params.cs, {});
      rb.query('suggestion', params.suggestion, {});
    }

    return this.http.request(rb.build({
      responseType: 'json',
      accept: 'application/json',
      context: params?.context
    })).pipe(
      filter((r: any) => r instanceof HttpResponse),
      map((r: HttpResponse<any>) => {
        return r as StrictHttpResponse<FindReviewResponse>;
      })
    );
  }

  /**
   * Find a random page and the replacements to review.
   *
   *
   *
   * This method provides access to only to the response body.
   * To access the full response (for headers, for example), `findRandomPageWithReplacements$Response()` instead.
   *
   * This method doesn't expect any request body.
   */
  findRandomPageWithReplacements(params?: {

    /**
     * Replacement kind code
     */
    kind?: number;

    /**
     * Replacement subtype
     */
    subtype?: string;

    /**
     * If the custom replacement is case-sensitive
     */
    cs?: boolean;

    /**
     * Custom replacement suggestion
     */
    suggestion?: string;
    context?: HttpContext
  }
): Observable<FindReviewResponse> {

    return this.findRandomPageWithReplacements$Response(params).pipe(
      map((r: StrictHttpResponse<FindReviewResponse>) => r.body as FindReviewResponse)
    );
  }

}
