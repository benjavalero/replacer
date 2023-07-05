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

import { DumpIndexingStatus } from '../models/dump-indexing-status';

@Injectable({ providedIn: 'root' })
export class DumpIndexingApiService extends BaseService {
  constructor(config: ApiConfiguration, http: HttpClient) {
    super(config, http);
  }

  /** Path part for operation `getDumpIndexingStatus()` */
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
  getDumpIndexingStatus$Response(
    params?: {
    },
    context?: HttpContext
  ): Observable<StrictHttpResponse<DumpIndexingStatus>> {
    const rb = new RequestBuilder(this.rootUrl, DumpIndexingApiService.GetDumpIndexingStatusPath, 'get');
    if (params) {
    }

    return this.http.request(
      rb.build({ responseType: 'json', accept: 'application/json', context })
    ).pipe(
      filter((r: any): r is HttpResponse<any> => r instanceof HttpResponse),
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
   * This method provides access only to the response body.
   * To access the full response (for headers, for example), `getDumpIndexingStatus$Response()` instead.
   *
   * This method doesn't expect any request body.
   */
  getDumpIndexingStatus(
    params?: {
    },
    context?: HttpContext
  ): Observable<DumpIndexingStatus> {
    return this.getDumpIndexingStatus$Response(params, context).pipe(
      map((r: StrictHttpResponse<DumpIndexingStatus>): DumpIndexingStatus => r.body)
    );
  }

  /** Path part for operation `manualStartDumpIndexing()` */
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
  manualStartDumpIndexing$Response(
    params?: {
    },
    context?: HttpContext
  ): Observable<StrictHttpResponse<void>> {
    const rb = new RequestBuilder(this.rootUrl, DumpIndexingApiService.ManualStartDumpIndexingPath, 'post');
    if (params) {
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
   * Start manually a dump indexing.
   *
   *
   *
   * This method provides access only to the response body.
   * To access the full response (for headers, for example), `manualStartDumpIndexing$Response()` instead.
   *
   * This method doesn't expect any request body.
   */
  manualStartDumpIndexing(
    params?: {
    },
    context?: HttpContext
  ): Observable<void> {
    return this.manualStartDumpIndexing$Response(params, context).pipe(
      map((r: StrictHttpResponse<void>): void => r.body)
    );
  }

}
