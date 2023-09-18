/* tslint:disable */
/* eslint-disable */
import { HttpClient, HttpContext, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { filter, map } from 'rxjs/operators';

import { BaseService } from '../base-service';
import { ApiConfiguration } from '../api-configuration';
import { StrictHttpResponse } from '../strict-http-response';
import { RequestBuilder } from '../request-builder';

import { Page } from '../models/page';
import { ReviewedPage } from '../models/reviewed-page';

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
  findPageReviewById$Response(
    params: {

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
    },
    context?: HttpContext
  ): Observable<StrictHttpResponse<Page>> {
    const rb = new RequestBuilder(this.rootUrl, PageApiService.FindPageReviewByIdPath, 'get');
    if (params) {
      rb.path('id', params.id, {});
      rb.query('kind', params.kind, {});
      rb.query('subtype', params.subtype, {});
      rb.query('cs', params.cs, {});
      rb.query('suggestion', params.suggestion, {});
    }

    return this.http.request(
      rb.build({ responseType: 'json', accept: 'application/json', context })
    ).pipe(
      filter((r: any): r is HttpResponse<any> => r instanceof HttpResponse),
      map((r: HttpResponse<any>) => {
        return r as StrictHttpResponse<Page>;
      })
    );
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
  findPageReviewById(
    params: {

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
    },
    context?: HttpContext
  ): Observable<Page> {
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
  saveReview$Response(
    params: {

    /**
     * Page ID
     */
      id: number;
      body: ReviewedPage
    },
    context?: HttpContext
  ): Observable<StrictHttpResponse<void>> {
    const rb = new RequestBuilder(this.rootUrl, PageApiService.SaveReviewPath, 'post');
    if (params) {
      rb.path('id', params.id, {});
      rb.body(params.body, 'application/json');
    }

    return this.http.request(
      rb.build({ responseType: 'text', accept: '*/*', context })
    ).pipe(
      filter((r: any): r is HttpResponse<any> => r instanceof HttpResponse),
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
   * This method provides access only to the response body.
   * To access the full response (for headers, for example), `saveReview$Response()` instead.
   *
   * This method sends `application/json` and handles request body of type `application/json`.
   */
  saveReview(
    params: {

    /**
     * Page ID
     */
      id: number;
      body: ReviewedPage
    },
    context?: HttpContext
  ): Observable<void> {
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
  findRandomPageWithReplacements$Response(
    params?: {

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
    },
    context?: HttpContext
  ): Observable<StrictHttpResponse<Page>> {
    const rb = new RequestBuilder(this.rootUrl, PageApiService.FindRandomPageWithReplacementsPath, 'get');
    if (params) {
      rb.query('kind', params.kind, {});
      rb.query('subtype', params.subtype, {});
      rb.query('cs', params.cs, {});
      rb.query('suggestion', params.suggestion, {});
    }

    return this.http.request(
      rb.build({ responseType: 'json', accept: 'application/json', context })
    ).pipe(
      filter((r: any): r is HttpResponse<any> => r instanceof HttpResponse),
      map((r: HttpResponse<any>) => {
        return r as StrictHttpResponse<Page>;
      })
    );
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
  findRandomPageWithReplacements(
    params?: {

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
    },
    context?: HttpContext
  ): Observable<Page> {
    return this.findRandomPageWithReplacements$Response(params, context).pipe(
      map((r: StrictHttpResponse<Page>): Page => r.body)
    );
  }

}
