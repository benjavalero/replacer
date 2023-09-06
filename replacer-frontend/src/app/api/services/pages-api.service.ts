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

import { KindCount } from '../models/kind-count';

@Injectable({ providedIn: 'root' })
export class PagesApiService extends BaseService {
  constructor(config: ApiConfiguration, http: HttpClient) {
    super(config, http);
  }

  /** Path part for operation `findPagesToReviewByType()` */
  static readonly FindPagesToReviewByTypePath = '/api/page/type';

  /**
   * List the pages to review containing the given replacement type.
   *
   *
   *
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `findPagesToReviewByType()` instead.
   *
   * This method doesn't expect any request body.
   */
  findPagesToReviewByType$Response(
    params: {
      lang: string;

    /**
     * Replacement kind code
     */
      kind: number;

    /**
     * Replacement subtype
     */
      subtype: string;
    },
    context?: HttpContext
  ): Observable<StrictHttpResponse<string>> {
    const rb = new RequestBuilder(this.rootUrl, PagesApiService.FindPagesToReviewByTypePath, 'get');
    if (params) {
      rb.query('lang', params.lang, {});
      rb.query('kind', params.kind, {});
      rb.query('subtype', params.subtype, {});
    }

    return this.http.request(
      rb.build({ responseType: 'text', accept: 'text/plain', context })
    ).pipe(
      filter((r: any): r is HttpResponse<any> => r instanceof HttpResponse),
      map((r: HttpResponse<any>) => {
        return r as StrictHttpResponse<string>;
      })
    );
  }

  /**
   * List the pages to review containing the given replacement type.
   *
   *
   *
   * This method provides access only to the response body.
   * To access the full response (for headers, for example), `findPagesToReviewByType$Response()` instead.
   *
   * This method doesn't expect any request body.
   */
  findPagesToReviewByType(
    params: {
      lang: string;

    /**
     * Replacement kind code
     */
      kind: number;

    /**
     * Replacement subtype
     */
      subtype: string;
    },
    context?: HttpContext
  ): Observable<string> {
    return this.findPagesToReviewByType$Response(params, context).pipe(
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
  reviewPagesByType$Response(
    params: {

    /**
     * Replacement kind code
     */
      kind: number;

    /**
     * Replacement subtype
     */
      subtype: string;
    },
    context?: HttpContext
  ): Observable<StrictHttpResponse<void>> {
    const rb = new RequestBuilder(this.rootUrl, PagesApiService.ReviewPagesByTypePath, 'post');
    if (params) {
      rb.query('kind', params.kind, {});
      rb.query('subtype', params.subtype, {});
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
   * Mark as reviewed the pages containing the given replacement type.
   *
   *
   *
   * This method provides access only to the response body.
   * To access the full response (for headers, for example), `reviewPagesByType$Response()` instead.
   *
   * This method doesn't expect any request body.
   */
  reviewPagesByType(
    params: {

    /**
     * Replacement kind code
     */
      kind: number;

    /**
     * Replacement subtype
     */
      subtype: string;
    },
    context?: HttpContext
  ): Observable<void> {
    return this.reviewPagesByType$Response(params, context).pipe(
      map((r: StrictHttpResponse<void>): void => r.body)
    );
  }

  /** Path part for operation `countPagesNotReviewedByType()` */
  static readonly CountPagesNotReviewedByTypePath = '/api/page/type/count';

  /**
   * Count the pages to review grouped by type (kind-subtype).
   *
   *
   *
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `countPagesNotReviewedByType()` instead.
   *
   * This method doesn't expect any request body.
   */
  countPagesNotReviewedByType$Response(
    params?: {
    },
    context?: HttpContext
  ): Observable<StrictHttpResponse<Array<KindCount>>> {
    const rb = new RequestBuilder(this.rootUrl, PagesApiService.CountPagesNotReviewedByTypePath, 'get');
    if (params) {
    }

    return this.http.request(
      rb.build({ responseType: 'json', accept: 'application/json', context })
    ).pipe(
      filter((r: any): r is HttpResponse<any> => r instanceof HttpResponse),
      map((r: HttpResponse<any>) => {
        return r as StrictHttpResponse<Array<KindCount>>;
      })
    );
  }

  /**
   * Count the pages to review grouped by type (kind-subtype).
   *
   *
   *
   * This method provides access only to the response body.
   * To access the full response (for headers, for example), `countPagesNotReviewedByType$Response()` instead.
   *
   * This method doesn't expect any request body.
   */
  countPagesNotReviewedByType(
    params?: {
    },
    context?: HttpContext
  ): Observable<Array<KindCount>> {
    return this.countPagesNotReviewedByType$Response(params, context).pipe(
      map((r: StrictHttpResponse<Array<KindCount>>): Array<KindCount> => r.body)
    );
  }

}
