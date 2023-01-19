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

import { DumpIndexingStatus } from '../models/dump-indexing-status';

@Injectable({
  providedIn: 'root',
})
export class DumpIndexingService extends BaseService {
  constructor(
    config: ApiConfiguration,
    http: HttpClient
  ) {
    super(config, http);
  }

  /**
   * Path part for operation getDumpIndexingStatus
   */
  static readonly GetDumpIndexingStatusPath = '/api/dump-indexing';

  /**
   * Find the status of the current (or the last) dump indexing.
   *
   *
   *
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `getDumpIndexingStatus()` instead.
   *
   * This method doesn't expect any request body.
   */
  getDumpIndexingStatus$Response(params?: {
    context?: HttpContext
  }
): Observable<StrictHttpResponse<DumpIndexingStatus>> {

    const rb = new RequestBuilder(this.rootUrl, DumpIndexingService.GetDumpIndexingStatusPath, 'get');
    if (params) {
    }

    return this.http.request(rb.build({
      responseType: 'json',
      accept: 'application/json',
      context: params?.context
    })).pipe(
      filter((r: any) => r instanceof HttpResponse),
      map((r: HttpResponse<any>) => {
        return r as StrictHttpResponse<DumpIndexingStatus>;
      })
    );
  }

  /**
   * Find the status of the current (or the last) dump indexing.
   *
   *
   *
   * This method provides access to only to the response body.
   * To access the full response (for headers, for example), `getDumpIndexingStatus$Response()` instead.
   *
   * This method doesn't expect any request body.
   */
  getDumpIndexingStatus(params?: {
    context?: HttpContext
  }
): Observable<DumpIndexingStatus> {

    return this.getDumpIndexingStatus$Response(params).pipe(
      map((r: StrictHttpResponse<DumpIndexingStatus>) => r.body as DumpIndexingStatus)
    );
  }

  /**
   * Path part for operation manualStartDumpIndexing
   */
  static readonly ManualStartDumpIndexingPath = '/api/dump-indexing';

  /**
   * Start manually a dump indexing.
   *
   *
   *
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `manualStartDumpIndexing()` instead.
   *
   * This method doesn't expect any request body.
   */
  manualStartDumpIndexing$Response(params?: {
    context?: HttpContext
  }
): Observable<StrictHttpResponse<void>> {

    const rb = new RequestBuilder(this.rootUrl, DumpIndexingService.ManualStartDumpIndexingPath, 'post');
    if (params) {
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
   * Start manually a dump indexing.
   *
   *
   *
   * This method provides access to only to the response body.
   * To access the full response (for headers, for example), `manualStartDumpIndexing$Response()` instead.
   *
   * This method doesn't expect any request body.
   */
  manualStartDumpIndexing(params?: {
    context?: HttpContext
  }
): Observable<void> {

    return this.manualStartDumpIndexing$Response(params).pipe(
      map((r: StrictHttpResponse<void>) => r.body as void)
    );
  }

}
