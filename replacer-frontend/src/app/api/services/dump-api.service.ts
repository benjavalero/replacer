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

import { DumpStatus } from '../models/dump-status';

@Injectable({ providedIn: 'root' })
export class DumpApiService extends BaseService {
  constructor(config: ApiConfiguration, http: HttpClient) {
    super(config, http);
  }

  /** Path part for operation `getDumpStatus()` */
  static readonly GetDumpStatusPath = '/api/dump';

  /**
   * Find the status of the current (or the last) dump indexing.
   *
   *
   *
   * This method provides access to the full `HttpResponse`, allowing access to response headers.
   * To access only the response body, use `getDumpStatus()` instead.
   *
   * This method doesn't expect any request body.
   */
  getDumpStatus$Response(
    params?: {
    },
    context?: HttpContext
  ): Observable<StrictHttpResponse<DumpStatus>> {
    const rb = new RequestBuilder(this.rootUrl, DumpApiService.GetDumpStatusPath, 'get');
    if (params) {
    }

    return this.http.request(
      rb.build({ responseType: 'json', accept: 'application/json', context })
    ).pipe(
      filter((r: any): r is HttpResponse<any> => r instanceof HttpResponse),
      map((r: HttpResponse<any>) => {
        return r as StrictHttpResponse<DumpStatus>;
      })
    );
  }

  /**
   * Find the status of the current (or the last) dump indexing.
   *
   *
   *
   * This method provides access only to the response body.
   * To access the full response (for headers, for example), `getDumpStatus$Response()` instead.
   *
   * This method doesn't expect any request body.
   */
  getDumpStatus(
    params?: {
    },
    context?: HttpContext
  ): Observable<DumpStatus> {
    return this.getDumpStatus$Response(params, context).pipe(
      map((r: StrictHttpResponse<DumpStatus>): DumpStatus => r.body)
    );
  }

  /** Path part for operation `manualStartDumpIndexing()` */
  static readonly ManualStartDumpIndexingPath = '/api/dump';

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
    const rb = new RequestBuilder(this.rootUrl, DumpApiService.ManualStartDumpIndexingPath, 'post');
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
