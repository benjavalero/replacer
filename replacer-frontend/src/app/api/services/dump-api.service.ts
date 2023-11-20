/* tslint:disable */
/* eslint-disable */
import { HttpClient, HttpContext } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { BaseService } from '../base-service';
import { ApiConfiguration } from '../api-configuration';
import { StrictHttpResponse } from '../strict-http-response';

import { DumpStatus } from '../models/dump-status';
import { getDumpStatus } from '../fn/dump/get-dump-status';
import { GetDumpStatus$Params } from '../fn/dump/get-dump-status';
import { manualStartDumpIndexing } from '../fn/dump/manual-start-dump-indexing';
import { ManualStartDumpIndexing$Params } from '../fn/dump/manual-start-dump-indexing';

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
  getDumpStatus$Response(params?: GetDumpStatus$Params, context?: HttpContext): Observable<StrictHttpResponse<DumpStatus>> {
    return getDumpStatus(this.http, this.rootUrl, params, context);
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
  getDumpStatus(params?: GetDumpStatus$Params, context?: HttpContext): Observable<DumpStatus> {
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
  manualStartDumpIndexing$Response(params?: ManualStartDumpIndexing$Params, context?: HttpContext): Observable<StrictHttpResponse<void>> {
    return manualStartDumpIndexing(this.http, this.rootUrl, params, context);
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
  manualStartDumpIndexing(params?: ManualStartDumpIndexing$Params, context?: HttpContext): Observable<void> {
    return this.manualStartDumpIndexing$Response(params, context).pipe(
      map((r: StrictHttpResponse<void>): void => r.body)
    );
  }

}
